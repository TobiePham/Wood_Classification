package com.example.woody.ui.identify;


import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Size;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.woody.R;
import com.example.woody.api.ApiService;
import com.example.woody.entity.Wood;
import com.example.woody.ml.ConvertModelMobilenetv3244;
import com.example.woody.ui.LoadingDialog;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.checkerframework.checker.units.qual.A;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class IdentifyFragment extends Fragment{
    private Button captureBtn, retakeBtn, identifyBtn, libraryBtn;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private TextView predictText;
    private ImageView imageView;
    private int idDetected;
    private float percent;
    private Bitmap picSelected;
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://woody-5c79f-default-rtdb.asia-southeast1.firebasedatabase.app");
    private DatabaseReference myRef= database.getReference("Wood");
    private boolean connectNetwork=false;
    private ArrayList<Wood> dataOffline= new ArrayList<>();
    private LoadingDialog loadingDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_identify, container, false);
        captureBtn = view.findViewById(R.id.image_capture_button);
        imageView = view.findViewById(R.id.captured_image);
        retakeBtn = view.findViewById(R.id.retake_button);
        identifyBtn = view.findViewById(R.id.cbtn);
        libraryBtn = view.findViewById(R.id.library_button);
        predictText=view.findViewById(R.id.predict);
        loadingDialog= new LoadingDialog(view.getContext());

        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connectNetwork = true;
        }
        else{
            connectNetwork = false;
            getWoodOffline();
        }


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
              Bitmap bImage = RotateBitmap(picSelected, 90);
              classifyImage(bImage,view.getContext());
              if(!connectNetwork){
                  predictText.setVisibility(View.VISIBLE);
                 for (Wood item: dataOffline){
                     if(item.getId()==idDetected){
                         predictText.setText(item.getDisplayName());
                     }
                 }
              }
              else{
                  loadingDialog.showLoading();
                checkImageQuality(bImage);
              }
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
                predictText.setVisibility(View.INVISIBLE);
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

    private void getWoodFromFirebase(int idDetected, View view) {
        myRef.orderByChild("id").equalTo(idDetected).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Wood wood= snapshot.getValue(Wood.class);
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String woodDetailJson = gson.toJson(wood);
                bundle.putString("wood", woodDetailJson);
                bundle.putString("fragment", "Identify");
//                bundle.putParcelable("BitmapImage", picSelected);
                bundle.putString("percent",(percent*100)+"%");
                Navigation.findNavController(view).navigate(R.id.detailWoodFragment, bundle);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });

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
            predictText.setVisibility(View.INVISIBLE);
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

    private void checkImageQuality(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        checkImageApi(encoded);
    }
    private void checkImageApi(String base64){
        final String[] result = {""};
        ApiService.apiService.checkWoodImage((base64)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                loadingDialog.hideLoading();
                if(response.body().equals("0")){
                   showAlert();
                }
                else{
                    classifyImage(picSelected,getContext());
                    getWoodFromFirebase(idDetected,getView());
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.hideLoading();
                System.out.println("asdf");
            }
        });

    }

    private void showAlert() {
        AlertDialog.Builder alert= new AlertDialog.Builder(this.getContext());
        alert.setTitle("Cảnh báo!");
        alert.setMessage("Hình ảnh có thể không phải ảnh gỗ, bạn có muốn tiếp tục không?");
        alert.setNeutralButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                previewView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                retakeBtn.setVisibility(View.INVISIBLE);
                identifyBtn.setVisibility(View.INVISIBLE);
                captureBtn.setVisibility(View.VISIBLE);
                predictText.setVisibility(View.INVISIBLE);
            }
        });
        alert.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                classifyImage(picSelected,getContext());
                getWoodFromFirebase(idDetected,getView());
            }
        });
        alert.show();
    }

    private void classifyImage(Bitmap image, Context context) {
        try {


            ConvertModelMobilenetv3244 model = ConvertModelMobilenetv3244.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer b= convertBitmapToByteBuffer(image);
            inputFeature0.loadBuffer(b);


            // Runs model inference and gets result.
            ConvertModelMobilenetv3244.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Runs model inference and gets result.

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
            percent=confidences[maxPos];
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

    private void getWoodOffline() {
        String json = null;
        try {
            InputStream inputStream = getContext().getAssets().open("data.json");
            int size = inputStream.available();
            byte[] bbuffer = new byte[size];
            inputStream.read(bbuffer);
            inputStream.close();
            json = new String(bbuffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            dataOffline = gson.fromJson(json, WOOD_TYPE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final Type WOOD_TYPE = new TypeToken<ArrayList<Wood>>() {
    }.getType();
}