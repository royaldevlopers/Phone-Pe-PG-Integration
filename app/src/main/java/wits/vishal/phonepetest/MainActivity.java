package wits.vishal.phonepetest;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.phonepe.intent.sdk.api.B2BPGRequest;
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder;
import com.phonepe.intent.sdk.api.PhonePe;
import com.phonepe.intent.sdk.api.PhonePeInitException;
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private final String MERCHANT_ID = "PGTESTPAYUAT86";
    private final String MERCHANT_USER_ID = "MUID123";
    private final String SALT_KEY = "96434309-7796-489d-8924-ab56988a6076";
    private final String SALT_INDEX = "1";
    private final String API_ENDPOINT = "/pg/v1/pay";
    private final String CALLBACK_URL = "https://webhook.site/8aac0e89-c931-4090-be94-f0d9343cc1f0";
    private final String MOBILE_NUMBER = "6354019460";
    private final String AMOUNT = "10100";
    private final int B2B_PG_REQUEST_CODE = 777;

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        PhonePe.init(getApplicationContext(), PhonePeEnvironment.SANDBOX, MERCHANT_ID, null);

        String payload = createPayload();
        String encodedPayload = encodeToBase64(payload);
        String checksum = sha256(encodedPayload + API_ENDPOINT + SALT_KEY) + "###" + SALT_INDEX;
        Log.e(TAG, "onCreate: checksum : " + checksum);

        B2BPGRequest b2BPGRequest = new B2BPGRequestBuilder()
                .setData(encodedPayload)
                .setChecksum(checksum)
                .setUrl(API_ENDPOINT)
                .build();

        button.setOnClickListener(v -> {
            try {
                startActivityForResult(PhonePe.getImplicitIntent(
                                this, b2BPGRequest, ""),
                        B2B_PG_REQUEST_CODE);
            } catch (PhonePeInitException e) {
                Log.e(TAG, "onCreate: start exception. ", e);
            }

        });


    }

    public String createPayload() {
        JSONObject payload = new JSONObject();
        try {
            payload.put("merchantId", MERCHANT_ID);
            payload.put("merchantTransactionId", "MT7850590068188104");
            payload.put("merchantUserId", MERCHANT_USER_ID);
            payload.put("amount", AMOUNT);
            payload.put("callbackUrl", CALLBACK_URL);
            payload.put("mobileNumber", MOBILE_NUMBER);

            JSONObject paymentInstrument = new JSONObject();
            paymentInstrument.put("type", "PAY_PAGE");

            payload.put("paymentInstrument", paymentInstrument);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        Log.e(TAG, "createPayload: payload : " + payload);
        return payload.toString();
    }

    private String encodeToBase64(String input) {
        byte[] inputBytes = input.getBytes();
        String encoding = Base64.encodeToString(inputBytes, Base64.NO_WRAP);
        Log.e(TAG, "encodeToBase64: Encoded Payload : " + encoding);
        return encoding;
    }
}