package com.example.imagprocessor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'imagprocessor' library on application startup.
    static {
        System.loadLibrary("imagprocessor");
    }
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 2;

    private ImageView imageView;
    private Button buttonSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);

        buttonSelectImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });
//        public native String stringFromJNI();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                sendImageToNative(bitmap);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendImageToNative(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Convert Bitmap to ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        byte[] byteArray = buffer.array();

        // Call the native method
        byte[] processedImage = processImage(byteArray, width, height);
        if (processedImage != null) {
            Bitmap processedBitmap = createBitmapFromRGBA(processedImage, width, height);
            // Render the processed image in an ImageView
            renderProcessedImage(processedBitmap);
        }
    }

    private Bitmap createBitmapFromRGBA(byte[] rgbaData, int width, int height) {
        // Create an empty Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Wrap the byte array in a ByteBuffer and use it to populate the Bitmap
        ByteBuffer buffer = ByteBuffer.wrap(rgbaData);
        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }

    private void renderProcessedImage(Bitmap processedBitmap) {
        // Assuming you have an ImageView to display the processed image
        ImageView imageView = findViewById(R.id.processedImageView); // Replace with your actual ImageView reference
        imageView.setImageBitmap(processedBitmap);
    }

    public native byte[] processImage(byte[] imageData, int width, int height);
}
