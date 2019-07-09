package info.bcdev.librarysdkew.wallet;

import android.os.AsyncTask;
import android.util.Log;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

import info.bcdev.librarysdkew.interfaces.callback.CBSendingEther;
import info.bcdev.librarysdkew.interfaces.callback.CBSendingToken;
import info.bcdev.librarysdkew.smartcontract.TokenERC20;

public class SendingToken {

    private Credentials mCredentials;
    private Web3j mWeb3j;
    private String fromAddress;
    private String mValueGasPrice;
    private String mValueGasLimit;

    private CBSendingEther cbSendingEther;
    private CBSendingToken cbSendingToken;
    private static final String TAG = "SendingToken";

    private final static BigInteger GAS_LIMIT = BigInteger.valueOf(556680L);
    private final static BigInteger GAS_PRICE = BigInteger.valueOf(200000000000L);

    public SendingToken(Web3j web3j, Credentials credentials, String valueGasPrice, String valueGasLimit){
        mWeb3j = web3j;
        mCredentials = credentials;
        fromAddress = credentials.getAddress();
        mValueGasPrice = valueGasPrice;
        mValueGasLimit = valueGasLimit;
    }

    private BigInteger getGasPrice(){
        //BigInteger.valueOf(Long.valueOf(mValueGasPrice))
        return GAS_PRICE;
    }

    private BigInteger getGasLimit(){
        //BigInteger.valueOf(Long.valueOf(mValueGasLimit)
        return GAS_LIMIT;
    }

    public void Send(String smartContractAddress, String toAddress, String valueAmmount) {
        Log.d(TAG, "Send:scaddress "+smartContractAddress);
        Log.d(TAG, "Send:toaddress "+toAddress);
        Log.d(TAG, "Send:valueamount "+valueAmmount);
        new SendToken().execute(smartContractAddress,toAddress,valueAmmount);
    }

    private class SendToken extends AsyncTask<String,Void,TransactionReceipt> {

        @Override
        protected TransactionReceipt doInBackground(String... value) {
            Log.d(TAG, "doInBackground:value2 "+value[2]);
            BigInteger a = new BigInteger("10");
            Log.d(TAG, "doInBackground:10 "+BigInteger.TEN);
            for (int i = 0 ; i <17 ; i ++) {
                Log.d(TAG, "doInBackground:a "+a);
                a= a.multiply(BigInteger.TEN);
            }
            Log.d(TAG, "doInBackground:after a : "+a);

            BigInteger value2 =  BigInteger.valueOf(Integer.valueOf(value[2]));
            Log.d(TAG, "doInBackground:value2 " + value2);
            //long as = (long) (Long.parseLong(value[2]));
            //Log.d(TAG, "doInBackground:after multifle 10 :"+as);
            BigInteger ammount = a.multiply(value2);
            Log.d(TAG, "doInBackground:amount "+ammount);
            Log.d(TAG, "doInBackground:smartaddress "+value[0]);
            Log.d(TAG, "doInBackground:toaddress "+value[1]);
            System.out.println(getGasPrice());
            System.out.println(getGasLimit());

            TokenERC20 token = TokenERC20.load(value[0], mWeb3j, mCredentials, getGasPrice(), getGasLimit());
            Log.d(TAG, "doInBackground:load ");
            try {
                TransactionReceipt result = token.transfer(value[1], ammount).send();
                Log.d(TAG, "doInBackground:success "+result.getTransactionHash());
                return result;
            } catch (Exception e) {
                Log.d(TAG, "doInBackground:error "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(TransactionReceipt result) {
            super.onPostExecute(result);
            cbSendingToken.backSendToken(result);
        }
    }

    public void registerCallBackToken(CBSendingToken cbSendingToken){
        this.cbSendingToken = cbSendingToken;
    }

}
