package com.example.woody.ui.identify;

import static androidx.core.view.ViewCompat.getDisplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;

import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.ViewPort;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.woody.R;
import com.example.woody.ml.ConvertModelMobilenetv3244;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;


public class IdentifyFragment extends Fragment {
    private Button captureBtn, retakeBtn, identifyBtn, libraryBtn;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageView imageView;
    private int imageSize = 224;
    private float idDetected;
    private Bitmap picSelected;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_identify, container, false);
        captureBtn = view.findViewById(R.id.image_capture_button);
        imageView = view.findViewById(R.id.captured_image);
        retakeBtn = view.findViewById(R.id.retake_button);
        identifyBtn = view.findViewById(R.id.cbtn);
        libraryBtn = view.findViewById(R.id.library_button);

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
            }
        });
        libraryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI.normalizeScheme());
                startActivityForResult(i, 3);
            }
        });
        identifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classifyImage(picSelected, view.getContext());
                Bundle bundle = new Bundle();
                bundle.putFloat("key", idDetected);
                bundle.putParcelable("BitmapImage", picSelected);
                Navigation.findNavController(view).navigate(R.id.detailWoodFragment, bundle);
            }
        });

        retakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                retakeBtn.setVisibility(View.INVISIBLE);
                identifyBtn.setVisibility(View.INVISIBLE);
                captureBtn.setVisibility(View.VISIBLE);
            }
        });
        previewView = view.findViewById(R.id.viewFinder);

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(view.getContext());
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, getExecutor());


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && data != null) {
            previewView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            Uri selectImage = data.getData();
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(selectImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                picSelected = RotateBitmap(bitmap, 90);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            retakeBtn.setVisibility(View.VISIBLE);
            identifyBtn.setVisibility(View.VISIBLE);
            captureBtn.setVisibility(View.INVISIBLE);
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this.getContext());
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setTargetResolution(new Size(480, 360)).build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);


    }


    private void capturePhoto() {
        imageCapture.takePicture(getExecutor(), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                System.out.println(image.getWidth());
                System.out.println(image.getHeight());
                imageView.setVisibility(View.VISIBLE);
                Bitmap bImage = RotateBitmap(toBitmap(image), 90);
                imageView.setImageBitmap(bImage);
                captureBtn.setVisibility(View.INVISIBLE);
                retakeBtn.setVisibility(View.VISIBLE);
                identifyBtn.setVisibility(View.VISIBLE);
                picSelected = bImage;

                image.close();
            }
        });
    }

    private void classifyImage(Bitmap image, Context context) {
        try {
            ConvertModelMobilenetv3244 model = ConvertModelMobilenetv3244.newInstance(context);
            Bitmap resized= Bitmap.createScaledBitmap(image,224,224,false);
//            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
//            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 *1 * imageSize * imageSize * 3);
//            byteBuffer.order(ByteOrder.nativeOrder());
//
//            byteBuffer.order(ByteOrder.nativeOrder());
//
//            int[] intValues = new int[imageSize * imageSize];
//            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
//            int pixel = 0;
//            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
//            for(int i = 0; i < imageSize; i ++){
//                for(int j = 0; j < imageSize; j++){
//                    int val = intValues[pixel++]; // RGB
//                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
//                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
//                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
//                }
//            }
            ByteBuffer b= convertBitmapToByteBuffer(image);
            inputFeature0.loadBuffer(b);
//            ImageProcessor imageProcessor = new ImageProcessor.Builder()
//                    .add(new ResizeWithCropOrPadOp(224, 224))
//                    .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
//                    .build();
//
//            TensorImage tensorImage = new TensorImage((DataType.FLOAT32));
//            tensorImage.load(image);
//            tensorImage = imageProcessor.process(tensorImage);
//            Bitmap b2= tensorImage.getBitmap();
//            inputFeature0.loadBuffer(tensorImage.getBuffer());

            // Runs model inference and gets result.
            ConvertModelMobilenetv3244.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();

            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
                System.out.println((i+1) + "tỉ lệ" + confidences[i]);
            }
            idDetected = maxPos + 1;
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bp) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(Float.BYTES*224*224*3);
        imgData.order(ByteOrder.nativeOrder());
        Bitmap bitmap = Bitmap.createScaledBitmap(bp,224,224,false);
        int [] intValues = new int[224*62240];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Convert the image to floating point.
        int pixel = 0;

        for (int i = 0; i < 224; ++i) {
            for (int j = 0; j < 224; ++j) {
                final int val = intValues[pixel++];

                imgData.putFloat(((val>> 16) & 0xFF) / 255.f);
                imgData.putFloat(((val>> 8) & 0xFF) / 255.f);
                imgData.putFloat((val & 0xFF) / 255.f);
            }
        }
        return imgData;
    }
    private Bitmap toBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}