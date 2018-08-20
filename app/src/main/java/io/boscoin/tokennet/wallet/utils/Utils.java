package io.boscoin.tokennet.wallet.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.IntentCompat;
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

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.boscoin.tokennet.wallet.R;
import io.boscoin.tokennet.wallet.conf.Constants;
import io.boscoin.tokennet.wallet.crypt.AESCrypt;
import io.boscoin.tokennet.wallet.crypt.Base58;
import io.boscoin.tokennet.wallet.db.DataBases;
import io.boscoin.tokennet.wallet.db.DbOpenHelper;


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
        SimpleDateFormat dateLocalFormat =  new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

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

    private static final int AES_KEY_LENGTH = 64;
    private static final String PRE_FIX = "BOS";
    private static final String SU_FFIX = "A1";
    public static boolean isValidRecoveryKey(String key){

        if(key.length() < AES_KEY_LENGTH+5 || !key.startsWith(PRE_FIX) || !key.endsWith(SU_FFIX)){

            return false;
        }

        String parsing = key.substring(3);
        try{
            if(Base58.IsBase58Enc(parsing)){
                return true;
            }
        }catch (IllegalArgumentException e){

            return false;
        }

        return false;
    }

    public static SpannableStringBuilder dispayBalance(String bal){
        int pos = bal.indexOf(".");

        SpannableStringBuilder ssb = new SpannableStringBuilder(bal);
        if(pos == -1){
            ssb.setSpan(new StyleSpan(Typeface.BOLD),0, bal.length()-4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else{
            ssb.setSpan(new StyleSpan(Typeface.BOLD),0, pos+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

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


        int n = 0;
        for (int i = 0; i < name.length(); ++n) {
            int cp = name.codePointAt(i);
            i += Character.charCount(cp);

        }

        return n < MAX_NAME;
    }


    private static final int MIN_PASSWORD = 7;
    public static boolean isPasswordValid(String password) {


        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-+=]).{8,}$");

        Matcher match = p.matcher(password);

        if(match.matches()){
            return  true;
        }

        return false;
    }

    public static String fitDigit(String str)
    {

        int dpos = str.indexOf(".");

        if(dpos == -1){
            return str;
        }

        String fstr = String.format("%,d",Integer.parseInt(str.substring(0, dpos)));
        String lstr = str.substring(dpos+1, str.length());
        String dstr = str.substring(dpos, str.length());

        while (dstr.endsWith("0")){
            dstr = (dstr.substring(0, dstr.length() - 1));
        }

        while (lstr.endsWith("0")){
            lstr = (lstr.substring(0, lstr.length() - 1));
        }

        if(lstr.length() == 0){
            return (fstr);
        }

        return (fstr + dstr);
    }

    public static BigDecimal MoneyCalcualtion(String m1, String m2, int type) throws Exception {
        final int ADD = 1;
        final int SUB = 2;
        BigDecimal preNum = new BigDecimal(m1);
        BigDecimal postNum = new BigDecimal(m2);

        if(type == ADD){

          return preNum.add(postNum);
        } else if(type == SUB){
            return preNum.subtract(postNum);
        } else{
          throw new Exception("");
        }
    }

    public static BigDecimal MoneyCalcualtion(BigDecimal m1, BigDecimal m2, int type) throws Exception {
        final int ADD = 1;
        final int SUB = 2;
        BigDecimal preNum = m1;
        BigDecimal postNum = m2;

        if(type == ADD){

            return preNum.add(postNum);
        } else if(type == SUB){
            return preNum.subtract(postNum);
        } else{
            throw new Exception("");
        }
    }



    public static void changeLanguage(Context context, String lang){
        Locale mLocale = new Locale(lang);
        Configuration config = new Configuration();
        config.locale = mLocale;
        context.getResources().updateConfiguration(config, null);




    }

    public static void restartApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        System.exit(0);
    }



}
