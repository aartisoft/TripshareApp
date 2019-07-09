package com.example.tripshare.Token;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tripshare.LoginRegister.PrefConfig;
import com.example.tripshare.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import info.bcdev.librarysdkew.interfaces.callback.CBBip44;
import info.bcdev.librarysdkew.interfaces.callback.CBGetCredential;
import info.bcdev.librarysdkew.interfaces.callback.CBLoadSmartContract;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.LoadSmartContract;
import info.bcdev.librarysdkew.wallet.SendingToken;
import info.bcdev.librarysdkew.web3j.Initiate;

public class WalletPassword extends AppCompatActivity implements CBGetCredential, CBLoadSmartContract, CBBip44, CBSendingEther, CBSendingToken {
    Button qrbt, sendbt, refreshbt, receiveetherbt,tokensendbt,tokenreceivebt, checkwalletbt;
    EditText addresset,tokenet;
    ImageView qrimg;
    TextView nowethertx, tokennowtx;

    private String mNodeUrl = config.addressethnode(2);
    private String mSmartcontract = config.addresssmartcontract(1);

    private static final String TAG = "WalletPassword";
    //외부 저장소 읽고 쓰는 권한
    private static final int READ_EXSTORAGE_REQUEST = 24;
    private static final int WRITE_EXSTORAGE_REQUEST = 25;
    String[] permissionlist = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //지갑 경로
    private String walletPath;
    private File walletDir;
    //지갑 비밀번호
    String password = "seung";

    //지갑 경로랑 주소,
    String[] result;
    //지갑 private key
    private BigInteger myprivatekey;
    //지갑 파일이름
    String filename;
    //web3j
    private Web3j web3;
    private String othrersaddress;

    public PrefConfig prefConfig;
    //metamask에서 생성된 지갑에서 이더 가져오기 위한 private key
    private final static String PRIVATE_KEY = "C0BE164D9ACD9C4290661B95858A3F19156D35F4D4100F960255771ED6EB410D";
    private final static String OUTADDRESS ="0x376Eb2839e8F39aa6c510Fff226101b186B053F4";
    //이더 전송할 때 사용하는 gas price 6721975L price 20000000000L

    private final static BigInteger GAS_LIMIT = BigInteger.valueOf(556680L);
    private final static BigInteger GAS_PRICE = BigInteger.valueOf(200000000000L);

    //qr코드 비트맵 담을 바이트
    private byte[] qrimgbyte;

    //이더 전송과 충전할 때 사용하는 progressbar
    ProgressDialog ethergivereceivebar;

    //이더 전송과 충전의 종료를 사용자에게 알려주기 위해
    private Handler etherhandler;
    private Bitmap qrbitmap;
    private SendingToken sendingToken;
    Credentials mycredentials;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_password);

        setupBouncyCastle();

        tokennowtx = findViewById(R.id.suseung_tx_wallet);
        qrbt = findViewById(R.id.qr_bt_wallet);
        sendbt = findViewById(R.id.ethertransfer_bt_wallet);
        addresset = findViewById(R.id.address_et_wallet);
        qrimg = findViewById(R.id.qrwallet_img);
        refreshbt = findViewById(R.id.refresh_bt_wallet);
        nowethertx = findViewById(R.id.nowether_tx_wallet);
        receiveetherbt = findViewById(R.id.ether_receive_bt_wallet);
        tokensendbt = findViewById(R.id.token_bt_wallet);
        tokenreceivebt = findViewById(R.id.token_receive_bt_wallet);
        checkwalletbt = findViewById(R.id.check_bt_wallet);

       etherhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 2) {
                    //qr코드를 만들 었을 때
                    qrimg.setImageBitmap(qrbitmap);
                } else if (msg.what == 0) {
                    //이더를 주고 받는 대 성공했을 때
                    ethergivereceivebar.dismiss();
                    getnowether();
                } else {
                    //이더를 주고 받는 대 실패 했을 때
                    ethergivereceivebar.dismiss();
                }
            }
        };
        //이더 전송을 위한 infura 연결
        getWeb3j();

        //지갑을 기기에 만들기 위한 읽고 쓰는 권한 확인
        checkwritepermission();

        //기기에 저장된 지갑 있는지 확인
        getwallet();

        //기기 지갑에 있는 이더 총 수 확인
        refreshbt.setOnClickListener(v -> getnowether());

        //1이더 내 지갑에 충전
        receiveetherbt.setOnClickListener((View v) -> receiveether());

        //이더 보낼 주소를 가져올 qr코드 reader
        qrbt.setOnClickListener(v -> qrreader());

        //이더 지정한 주소로 보내기
        sendbt.setOnClickListener(v -> sendtoothers());

        //원하는 토큰만큼 충전
        tokenreceivebt.setOnClickListener(v->receivetoken());
                //tokenrecieve()

        //토큰 지정한 계좌로 전송
        tokensendbt.setOnClickListener(v->sendToken());

        //내 지갑 거래 내역 확인
        checkwalletbt.setOnClickListener(v->checkwallet());

        //지갑에 있는토큰 개수 받기
        GetTokenInfo();
    }

    private void checkwallet() {
    //내 주소를 다음 화면으로 보내기
    Intent intent = new Intent(WalletPassword.this, CheckWallet.class);
    intent.putExtra("address",result[1]);
    startActivity(intent);

    }


    private void receivetoken() {

        Log.d(TAG, "receivetoken:myaddress "+result[1]);
        final EditText tokenamountet = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(WalletPassword.this);
        builder.setTitle("토큰 충전");
        builder.setMessage("충전할 토큰의 개수를 입력해 주세요.");
        builder.setView(tokenamountet);
        builder.setPositiveButton("확인",
                (dialog, which) -> {
                    //토큰 입력 개수
                    String tokenamount = tokenamountet.getText().toString();
                    Log.d(TAG, "receivetoken:충전 토큰 수"+tokenamount);

                    ethergivereceivebar = new ProgressDialog(WalletPassword.this);
                    ethergivereceivebar.setMessage(tokenamount+" 토큰 충전 중입니다.");
                    ethergivereceivebar.show();

                    //토큰 보내기
                    sendingToken = new SendingToken(web3,
                            getCredentialsFromPrivavteKey(),
                            GAS_PRICE.toString(),
                            GAS_LIMIT.toString());
                    sendingToken.registerCallBackToken(this);
                    sendingToken.Send(mSmartcontract,result[1],tokenamount);
                });
        builder.setNegativeButton("취소",
                (dialog, which) -> {
                });
        builder.show();

    }

    /* Get Web3j*/
    private void getWeb3j(){
        Log.d(TAG, "getWeb3j:nodeurl "+mNodeUrl);
        new Initiate(mNodeUrl);
        web3 = Initiate.sWeb3jInstance;
    }

    private void sendToken(){

        final EditText tokenamountet = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(WalletPassword.this);
        builder.setTitle("토큰 보내기");
        builder.setMessage("보낼 토큰의 개수를 입력해 주세요.");
        builder.setView(tokenamountet);
        builder.setPositiveButton("확인",
                (dialog, which) -> {
                    //토큰 입력 개수
                    String tokenamount = tokenamountet.getText().toString();

                    ethergivereceivebar = new ProgressDialog(WalletPassword.this);
                    ethergivereceivebar.setMessage(tokenamount+" 토큰 전송 중입니다.");
                    ethergivereceivebar.show();
                    Log.d(TAG, "sendToken:보낼 토큰 수 "+tokenamount);
                    //토큰 보내기
                    sendingToken = new SendingToken(web3,
                            mycredentials,
                            GAS_PRICE.toString(),
                            GAS_LIMIT.toString());
                    sendingToken.registerCallBackToken(this);
                    sendingToken.Send(mSmartcontract,getToAddress(),tokenamount);
                });
        builder.setNegativeButton("취소",
                (dialog, which) -> {
                });
        builder.show();
    }

    private String getSendTokenAmmount() {
        return tokenet.getText().toString();
    }

    private String getToAddress(){return addresset.getText().toString();}

    public void getBalance() {

        String result = null;
        EthGetBalance ethGetBalance = null;
        try {
            //이더리움 노드에게 지정한 Address 의 잔액을 조회한다.
            ethGetBalance = web3.ethGetBalance(OUTADDRESS, DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger wei = ethGetBalance.getBalance();
            Log.d(TAG, "getBalance:ethgetbalance "+ethGetBalance.toString());
            //wei 단위를 ETH 단위로 변환 한다.
            result = Convert.fromWei(wei.toString(), Convert.Unit.ETHER).toString();
            Log.d(TAG, "getBalance:result " + result);
        } catch (InterruptedException e) {
            Log.d(TAG, "getBalance:error ");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d(TAG, "getBalance:error ");
            e.printStackTrace();
        }
//        status = "check";
//        new checktoken().execute();
    }

    /*private void tokenrecieve() {

        //이더 전송과 충전할 때 사용하는 progressbar
        ethergivereceivebar = new ProgressDialog(WalletPassword.this);
        ethergivereceivebar.setMessage("100토큰 충전 중입니다.");
        ethergivereceivebar.show();
        Log.d(TAG, "receiveether:before thread ");
        //외부 지갑에서 토큰을 받는 것
        new Thread(() -> {

            EthGetTransactionCount ethGetTransactionCount = null;
            try {
                ethGetTransactionCount = web3.ethGetTransactionCount(
                        OUTADDRESS, DefaultBlockParameterName.LATEST).sendAsync().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
           *//* RawTransactionManager rawTransactionManager = new RawTransactionManager(
                    nonce,
                    getCredentialsFromPrivavteKey()
            );
*//*
            Transfer transfer = new Transfer(web3, transactionManager);
            Log.d(TAG, "receiveether:pre ");

            try {
                TransactionReceipt transactionReceipt = transfer.sendFunds(
                        result[1],
                        BigDecimal.valueOf(100),
                        Convert.Unit.fromString("Seung"),
                        GAS_PRICE,
                        GAS_LIMIT
                ).send();

                Message message = etherhandler.obtainMessage();
                //이더 전송에 성공했다는 뜻
                message.what = 0;
                etherhandler.sendMessage(message);
                Log.d(TAG, "receiveether:success ");
                Log.d(TAG, "receiveether:receipt "+transactionReceipt.getTransactionHash());
            } catch (Exception e) {

                Message message = etherhandler.obtainMessage();
                //이더 전송에 실패했다는 뜻
                message.what = 1;
                etherhandler.sendMessage(message);
                Log.d(TAG, "receiveether: " + e.getMessage());
                e.printStackTrace();
            }

        }).start();
    }*/

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    private void sendtoothers() {
        if (addresset.getText().toString().equals("")) {
            //주소에 아무것도 입력 되지 않으면 종료
            return;
        }
        //진행 바 보여주기
        //이더 전송과 충전할 때 사용하는 progressbar
        ethergivereceivebar = new ProgressDialog(WalletPassword.this);
        ethergivereceivebar.setMessage("1 이더 전송 중입니다.");
        ethergivereceivebar.show();

        //이더를 받을 사람의 지갑 주소
        othrersaddress = addresset.getText().toString();

        Log.d(TAG, "sendtoothers:before thread ");
        new Thread(() -> {
            Credentials credentials = null;
            try {
                credentials = WalletUtils.loadCredentials(password, result[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
            }
            TransactionManager transactionManager = new RawTransactionManager(
                    web3,
                    credentials
            );

            Transfer transfer = new Transfer(web3, transactionManager);
            Log.d(TAG, "sendtoothers:pre ");

            try {
                TransactionReceipt transactionReceipt = transfer.sendFunds(
                        othrersaddress,
                        BigDecimal.ONE,
                        Convert.Unit.ETHER,
                        GAS_PRICE,
                        GAS_LIMIT
                ).send();

                Message message = etherhandler.obtainMessage();
                //이더 전송에 성공했다는 뜻
                message.what = 0;
                etherhandler.sendMessage(message);
                Log.d(TAG, "sendtoothers:success ");
                Log.d(TAG, "v " + transactionReceipt.getTransactionHash());
            } catch (Exception e) {
                Message message = etherhandler.obtainMessage();
                //이더 전송에 실패했다는 뜻
                message.what = 1;
                etherhandler.sendMessage(message);
                Log.d(TAG, "sendtootherts " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }


    public void qrreader() {
        new IntentIntegrator(this).initiateScan();
    }

    private void receiveether() {
        //이더 전송과 충전할 때 사용하는 progressbar
        ethergivereceivebar = new ProgressDialog(WalletPassword.this);
        ethergivereceivebar.setMessage("1 이더 충전 중입니다.");
        ethergivereceivebar.show();
        Log.d(TAG, "receiveether:before thread ");
        //외부 지갑에서 토큰을 받는 것
        new Thread(() -> {
            TransactionManager transactionManager = new RawTransactionManager(
                    web3,
                    getCredentialsFromPrivavteKey()
            );

            Transfer transfer = new Transfer(web3, transactionManager);
            Log.d(TAG, "receiveether:pre ");

            try {
                TransactionReceipt transactionReceipt = transfer.sendFunds(
                        result[1],
                        BigDecimal.ONE,
                        Convert.Unit.ETHER,
                        GAS_PRICE,
                        GAS_LIMIT
                ).send();

                Message message = etherhandler.obtainMessage();
                //이더 전송에 성공했다는 뜻
                message.what = 0;
                etherhandler.sendMessage(message);
                Log.d(TAG, "receiveether:success ");
                Log.d(TAG, "receiveether:receipt "+transactionReceipt.getTransactionHash());
            } catch (Exception e) {

                Message message = etherhandler.obtainMessage();
                //이더 전송에 실패했다는 뜻
                message.what = 1;
                etherhandler.sendMessage(message);
                Log.d(TAG, "receiveether: " + e.getMessage());
                e.printStackTrace();
            }

        }).start();
    }

    private Credentials getCredentialsFromPrivavteKey() {
        return Credentials.create(PRIVATE_KEY);
    }

    private void getnowether() {
        String innerether = null;
        EthGetBalance ethGetBalance = null;
        try {
            //이더리움 노드에게 지정한 Address의 잔액을 조회한다.
            ethGetBalance = web3.ethGetBalance(result[1], DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger wei = ethGetBalance.getBalance();

            //wei 단위를 ETH 단위로 변환 한다.
            innerether = Convert.fromWei(wei.toString(), Convert.Unit.ETHER).toString();
            Log.d(TAG, "getBalance:result " + innerether);
            String nowether = "이더 총 개수 : " + innerether;
            //이더 개수 사용자에게 보여 주기
            nowethertx.setText(nowether);

            //TOken도 조회한다.
            GetTokenInfo();
        } catch (InterruptedException e) {
            Log.d(TAG, "getBalance:error ");
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void getwallet() {
        prefConfig = new PrefConfig(this);

        Log.d(TAG, "getwallet: :" + prefConfig.readwallet());
        String saveornot = prefConfig.readwallet();
        if (saveornot.equals("")) {
            Log.d(TAG, "getwallet:make ");
            //지갑 생성을 안했다면 지갑 생성하기, qr코드도 같이 생성
            makemywallet();
        } else {
            Log.d(TAG, "getwallet:get ");
            //지갑을 생성했다면 지갑의 주소 가져오기
            getmywallet();
            //지갑의 이더 가져오기
            getnowether();
        }
    }

    private void getmywallet() {
        filename = prefConfig.readwallet();
        Log.d(TAG, "makemywallet:path " + filename);

        result = new String[2];
        //지갑의 이름을 담는다.
        result[0] = filename;
        try {
            mycredentials = WalletUtils.loadCredentials(password, result[0]);
            Log.d(TAG, "makemywallet:publick key " + mycredentials.getEcKeyPair().getPublicKey());
            Log.d(TAG, "makemywallet:primary key " + mycredentials.getEcKeyPair().getPrivateKey());
            myprivatekey = mycredentials.getEcKeyPair().getPrivateKey();
            //1 > 지갑 주소
            result[1] = mycredentials.getAddress();
            Log.d(TAG, "makemywallet:address " + result[1]);

            //지갑에 해당하는 qr코드 이미지 가져오기
            String qrimgstr = prefConfig.readqrimg();
            //QR코드 이미지 문자열을 BITMAP으로 변경해 사용자에게 보여주기
            byte[] qrimgdecodedByteArray = Base64.decode(qrimgstr, Base64.DEFAULT);
            Bitmap qrimgdecoded = BitmapFactory.decodeByteArray(qrimgdecodedByteArray, 0, qrimgdecodedByteArray.length);
            qrimg.setImageBitmap(qrimgdecoded);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }

    private void checkwritepermission() {
        if (ContextCompat.checkSelfPermission(WalletPassword.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(WalletPassword.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(WalletPassword.this,
                    permissionlist, 2);
        }
    }

    private void makemywallet() {
        //지갑 경로가 될 절대 경로 구하기
        walletPath = getFilesDir().getAbsolutePath();
        //지갑을 넣을 파일 생성
        walletDir = new File(walletPath);

        new Thread(() -> {
            result = new String[2];
            try {
                //지갑생성
                String fileName = WalletUtils.generateLightNewWalletFile(password, new File(walletPath));
                //0 > 지갑 경로
                result[0] = walletPath + "/" + fileName;
                Log.d(TAG, "makemywallet:path " + result[0]);
                Credentials credentials = WalletUtils.loadCredentials(password, result[0]);
                Log.d(TAG, "makemywallet:publick key " + credentials.getEcKeyPair().getPublicKey());
                Log.d(TAG, "makemywallet:primary key " + credentials.getEcKeyPair().getPrivateKey());
                myprivatekey = credentials.getEcKeyPair().getPrivateKey();
                //1 지갑 주소
                result[1] = credentials.getAddress();
                Log.d(TAG, "makemywallet:address " + result[1]);

                //지갑 주소를 가질 qr 코드 만들기
                generateRQcode(result[1]);

                //지갑 파일 이름을 기기에 저장하기
                prefConfig.writewallet(result[0]);

            } catch (NoSuchAlgorithmException
                    | NoSuchProviderException
                    | InvalidAlgorithmParameterException
                    | IOException
                    | CipherException e) {
                e.printStackTrace();
                Log.d(TAG, "makemywallet:error " + e.getMessage());
            }
        }).start();
    }

    //qr 코드로 읽은 이더 보낼 주소 구하기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null) {
                // 취소됨
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // 스캔된 QRCode --> result.getContents()
                //Toast.makeText(this, " " + result.getContents(), Toast.LENGTH_LONG).show();
                othrersaddress = result.getContents();
                addresset.setText("");
                addresset.setText(othrersaddress);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //qr 코드 만들기
    private void generateRQcode(String address) {
        Log.d(TAG, "generateRQcode:address " + address);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            //qr코드로 만들고 높이랑 길이 정해줌
            qrbitmap = toBitmap(qrCodeWriter.encode(address, BarcodeFormat.QR_CODE, 200, 200));
            //qr코드 보여주기

            //메인 쓰레드로 메세지 보내기
            Message message = etherhandler.obtainMessage();
            message.what = 2;
            etherhandler.sendMessage(message);
            //qrimg.setImageBitmap(qrbitmap);
            //bitmap을 문자열로 바꿔줌
            //shared에 qr코드 이미지 저장
            Log.d(TAG, "generateRQcode:bit " + qrbitmap);
            prefConfig.writeqrimg(bitmaptostring(qrbitmap));

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    //qr코드를 bitmap으로 만들기
    private Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    private String bitmaptostring(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //bitmap을 jpeg로 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        qrimgbyte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(qrimgbyte, Base64.DEFAULT);
    }

    @Override
    public void backGeneration(Map<String, String> result, Credentials credentials) {
        Log.d(TAG, "backGeneration: ");
    }


    @Override
    public void backLoadCredential(Credentials credentials) {
        //토큰 정보 얻기
        GetTokenInfo();
    }

    @Override
    public void backLoadSmartContract(Map<String, String> result) {
        Log.d(TAG, "backLoadSmartContract:토큰 전송 갱신 ");
        Log.d(TAG, "backLoadSmartContract:balance "+result.get("tokenbalance"));
        Log.d(TAG, "backLoadSmartContract:supply "+result.get("totalsupply"));
        tokennowtx.setText("Seung 토큰 총 개수 : "+result.get("tokenbalance"));
    }

    @Override
    public void backSendEthereum(EthSendTransaction result) {
    }

    @Override
    public void backSendToken(TransactionReceipt result) {
        Log.d(TAG, "backSendToken:토큰 충전이나 다른 사람에게 전송 끝");
        Log.d(TAG, "backSendToken:토큰 갱신 ");
        GetTokenInfo();
        //다이어로그 종료
        ethergivereceivebar.dismiss();
    }

    //지갑의 토큰 개수를 가져옴
    private void GetTokenInfo() {
        LoadSmartContract loadSmartContract = new LoadSmartContract(web3,mycredentials,mSmartcontract,GAS_PRICE,GAS_LIMIT);
        loadSmartContract.registerCallBack(this);
        loadSmartContract.LoadToken();
    }
}
