package io.boscoin.toknenet.wallet.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import com.google.common.io.BaseEncoding;

import org.stellar.sdk.FormatException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.boscoin.toknenet.wallet.R;
import io.boscoin.toknenet.wallet.crypt.AESCrypt;


public class Utils {
    private static final String TAG = "Utils";


    public static String convertUtcToLocal(String utcTime){
        String localTime = "";

        SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat dateLocalFormat =  new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        try {

            Date dateUtcTime = dateFormat.parse(utcTime);

            long longUtcTime = dateUtcTime.getTime();

            TimeZone zone = TimeZone.getDefault();
            int offset = zone.getOffset(longUtcTime);
            long longLocalTime = longUtcTime + offset;


            Date dateLocalTime = new Date();
            dateLocalTime.setTime(longLocalTime);

            localTime = dateLocalFormat.format(dateLocalTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return localTime;
    }

    public static String getCreateTime(long createTime){
        String localTime = "";
        SimpleDateFormat dateLocalFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date dateLocalTime = new Date(createTime);
        localTime = dateLocalFormat.format(dateLocalTime);

        return localTime;
    }

    public static String contractionAddress(String add){
        if(add == null){
            return "Created";
        }

        String address;
        int length = add.length();
        int strat = length -4;
        address = add.substring(0,4); 

        address = address+" ... ";
        address = address+add.substring(strat,length);

        return address;
    }


    public enum VersionByte {
        ACCOUNT_ID((byte)(6 << 3)), // G
        SEED((byte)(18 << 3)), // S
        PRE_AUTH_TX((byte)(19 << 3)), // T
        SHA256_HASH((byte)(23 << 3)); // X
        private final byte value;
        VersionByte(byte value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    private static BaseEncoding base32Encoding = BaseEncoding.base32().upperCase().omitPadding();

    public static byte[] decodeCheck(VersionByte versionByte, char[] encoded) {
        byte[] bytes = new byte[encoded.length];
        for (int i = 0; i < encoded.length; i++) {
            if (encoded[i] > 127) {
                throw new IllegalArgumentException("Illegal characters in encoded char array.");
            }
            bytes[i] = (byte) encoded[i];
        }

        byte[] decoded = base32Encoding.decode(java.nio.CharBuffer.wrap(encoded));
        byte decodedVersionByte = decoded[0];
        byte[] payload  = Arrays.copyOfRange(decoded, 0, decoded.length-2);
        byte[] data     = Arrays.copyOfRange(payload, 1, payload.length);
        byte[] checksum = Arrays.copyOfRange(decoded, decoded.length-2, decoded.length);

        if (decodedVersionByte != versionByte.getValue()) {
            throw new FormatException("Version byte is invalid");
        }

        byte[] expectedChecksum = calculateChecksum(payload);

        if (!Arrays.equals(expectedChecksum, checksum)) {
            throw new FormatException("Checksum invalid");
        }

        if (VersionByte.SEED.getValue() == decodedVersionByte) {
            Arrays.fill(bytes, (byte) 0);
            Arrays.fill(decoded, (byte) 0);
            Arrays.fill(payload, (byte) 0);
        }

        return data;
    }

    private static byte[] calculateChecksum(byte[] bytes) {
        // This code calculates CRC16-XModem checksum
        // Ported from https://github.com/alexgorbatchev/node-crc
        int crc = 0x0000;
        int count = bytes.length;
        int i = 0;
        int code;

        while (count > 0) {
            code = crc >>> 8 & 0xFF;
            code ^= bytes[i++] & 0xFF;
            code ^= code >>> 4;
            crc = crc << 8 & 0xFFFF;
            crc ^= code;
            code = code << 5 & 0xFFFF;
            crc ^= code;
            code = code << 7 & 0xFFFF;
            crc ^= code;
            count--;
        }

        // little-endian
        return new byte[] {
                (byte)crc,
                (byte)(crc >>> 8)};
    }

    /**
     * Output the contents of the response to the console.
     * @param response the response from the Stellar Server
     */
    public static void printResponse(final SubmitTransactionResponse response)
    {
        System.out.println("Successful? " + response.isSuccess());
        System.out.println("Ledger# " + response.getLedger());
        System.out.println("envelope_xdr " +response.getEnvelopeXdr());
        System.out.println("result_xdr " +response.getResultXdr());

        final SubmitTransactionResponse.Extras extras = response.getExtras();
        if (extras != null) {
            System.out.println("TransactionResult: " + extras.getResultXdr());
            System.out.println("TransactionEnvelope: " + extras.getEnvelopeXdr());
        }
        else {
            if (!response.isSuccess()) {
                System.out.println("Extras = null"); //Extras are always null if the response is a success.
            }
        }
    }

    /**

     * This method converts dp unit to equivalent pixels, depending on device density.

     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels

     * @param context Context to get resources and device specific display metrics

     * @return A float value to represent px equivalent to dp depending on device density

     */

    public static int convertDpToPixel(float dp, Context context){

        Resources resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        float px = dp * (metrics.densityDpi / 160f);


        return (int)(Math.round(px));

    }



    /**

     * This method converts device specific pixels to density independent pixels.

     * @param px A value in px (pixels) unit. Which we need to convert into db

     * @param context Context to get resources and device specific display metrics

     * @return A float value to represent dp equivalent to px value

     */

    public static float convertPixelsToDp(float px, Context context){

        Resources resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        float dp = px / (metrics.densityDpi / 160f);

        return dp;

    }

    public static boolean isValidRecoveryKey(String key){
        String suffix = key.substring(key.length() - 2, key.length()-1);
        if(key.startsWith("BOS") && suffix.endsWith("A")){
            return true;
        }else{
            return false;
        }

    }

    public static SpannableStringBuilder dispayBalance(String bal){
        int pos = bal.indexOf(".");
        Log.e(TAG,"pos = "+pos);
        SpannableStringBuilder ssb = new SpannableStringBuilder(bal);
        ssb.setSpan(new StyleSpan(Typeface.BOLD),0, pos+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    public static SpannableStringBuilder changeColorRed(String str){

        int pos = str.indexOf("BOS");
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#db0303")), 0, pos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD),0, pos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    public static SpannableStringBuilder changeColorBlue(String str){

        int pos = str.indexOf("BOS");
        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#0082f2")), 0, pos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.BOLD),0, pos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private static final int MAX_NAME = 11;
    public static boolean isNameValid(String name) {
        return name.length() < MAX_NAME;
    }


    private static final int MIN_PASSWORD = 7;
    public static boolean isPasswordValid(String password) {

        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");
        Matcher match = p.matcher(password);

        if(match.matches() && password.length() > MIN_PASSWORD){
            return  true;
        }

        return false;
    }

    public static String fitDigit(String str)
    {
        int dpos = str.indexOf(".");
        String dstr = String.format("%,d",Integer.parseInt(str.substring(0, dpos)));
        String fstr = str.substring(dpos+1, str.length());
        while (fstr.endsWith("0")){
            fstr = (fstr.substring(0, fstr.length() - 1));
        }

        return (dstr + fstr);
    }

 


}
