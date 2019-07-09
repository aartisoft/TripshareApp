package com.example.tripshare.Token;

public class config {

    public static String addressethnode(int node) {
        switch(node){
            case 1:
                return "http://176.74.13.102:18087";
            case 2:
                return "https://ropsten.infura.io/v3/8c2b6fb03aef4d2a906e2bace01fbd7b";
            default:
                        return "https://ropsten.infura.io/v3/8c2b6fb03aef4d2a906e2bace01fbd7b";
        }
    }

    public static String addresssmartcontract(int contract) {
        switch (contract){
            case 1:
                return "0x28Cb68Ce7cFE8BD45B86be7e469d08c7A3bbf266";
            default :
                return "0x28Cb68Ce7cFE8BD45B86be7e469d08c7A3bbf266";
        }
    }

    public static String passwordwallet() {
        return "";
    }


}
