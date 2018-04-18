package io.boscoin.toknenet.wallet;

import org.junit.Test;
import org.stellar.sdk.Account;
import org.stellar.sdk.AccountMergeOperation;
import org.stellar.sdk.AllowTrustOperation;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetCodeLengthInvalidException;
import org.stellar.sdk.AssetTypeCreditAlphaNum12;
import org.stellar.sdk.AssetTypeCreditAlphaNum4;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.CreateAccountOperation;
import org.stellar.sdk.CreatePassiveOfferOperation;
import org.stellar.sdk.FormatException;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.ManageDataOperation;
import org.stellar.sdk.ManageOfferOperation;
import org.stellar.sdk.Memo;
import org.stellar.sdk.Network;
import org.stellar.sdk.Operation;
import org.stellar.sdk.PathPaymentOperation;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Price;
import org.stellar.sdk.Server;
import org.stellar.sdk.SetOptionsOperation;
import org.stellar.sdk.Signer;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.xdr.SignerKey;

import java.io.IOException;
import java.util.Arrays;


import okhttp3.internal.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperationTest {
    @Test
    public void testCreateAccountOperation() throws FormatException, IOException, AssetCodeLengthInvalidException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair destination = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");

        String startingAmount = "1000";
        CreateAccountOperation operation = new CreateAccountOperation.Builder(destination, startingAmount)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        CreateAccountOperation parsedOperation = (CreateAccountOperation) Operation.fromXdr(xdr);

        assertEquals(10000000000L, xdr.getBody().getCreateAccountOp().getStartingBalance().getInt64().longValue());
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());
        assertEquals(destination.getAccountId(), parsedOperation.getDestination().getAccountId());
        assertEquals(startingAmount, parsedOperation.getStartingBalance());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAAAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAACVAvkAA==",
                operation.toXdrBase64());
    }

    @Test
    public void testBuilderSuccessTestnet() throws FormatException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .build();

        transaction.sign(source);

        assertEquals(
                "AAAAAF7FIiDToW1fOYUFBC0dmyufJbFTOa2GQESGz+S2h5ViAAAAZAAKVaMAAAABAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAAEqBfIAAAAAAAAAAABtoeVYgAAAEDLki9Oi700N60Lo8gUmEFHbKvYG4QSqXiLIt9T0ru2O5BphVl/jR9tYtHAD+UeDYhgXNgwUxqTEu1WukvEyYcD",
                transaction.toEnvelopeXdrBase64());

        assertEquals(transaction.getSourceAccount(), source);
        assertEquals(transaction.getSequenceNumber(), sequenceNumber+1);
        assertEquals(transaction.getFee(), 100);
    }

    @Test
    public void testCreateAccount() throws IOException {
        final Server stellarServer = new Server("https://horizon-testnet.stellar.org");

        // Create a public and private key for the new account.
        final KeyPair newAccountKeyPair = KeyPair.random();
        System.out.println("New account secret seed: " + newAccountKeyPair.getSecretSeed());

        // Get the private key and the ledger number for the existing account.
        final KeyPair existingAccountKeyPair = KeyPair.fromSecretSeed("SBB4LZ6OHDFNPGOBVUW3VJ4YJPYMJ7GJM5RBYTD6L7YPMSUGVMLLI5LK");
        final AccountResponse existingAccount = stellarServer.accounts().account(existingAccountKeyPair);

        //Create a transaction for the existing account which sets up the new account.
        final Transaction.Builder transactionBuilder = new Transaction.Builder(existingAccount);

        final CreateAccountOperation createAccountOperation =
                new CreateAccountOperation.Builder(newAccountKeyPair, Integer.toString(20)).
                        setSourceAccount(existingAccountKeyPair).build();

        transactionBuilder.addOperation(createAccountOperation);
        transactionBuilder.addMemo(Memo.text("ExampleAccount"));

        final Transaction createAccountTransaction = transactionBuilder.build();

        //Sign this transaction with the new account, and then submit it to the stellar server.
        createAccountTransaction.sign(existingAccountKeyPair);

        System.out.println("Creating account...");
        final SubmitTransactionResponse createAccountResponse = stellarServer.submitTransaction(createAccountTransaction);
        printResponse(createAccountResponse);
    }

    private void printResponse(final SubmitTransactionResponse response) {
        System.out.println("Successful? " + response.isSuccess());
        System.out.println("Ledger# " + response.getLedger());

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
    }
/*    @Test
    public void testPaymentOperation() throws FormatException, IOException, AssetCodeLengthInvalidException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair destination = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");

        Asset asset = new AssetTypeNative();
        String amount = "1000";

        PaymentOperation operation = new PaymentOperation.Builder(destination, asset, amount)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        PaymentOperation parsedOperation = (PaymentOperation) Operation.fromXdr(xdr);

        assertEquals(10000000000L, xdr.getBody().getPaymentOp().getAmount().getInt64().longValue());
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());
        assertEquals(destination.getAccountId(), parsedOperation.getDestination().getAccountId());
        assertTrue(parsedOperation.getAsset() instanceof AssetTypeNative);
        assertEquals(amount, parsedOperation.getAmount());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAEAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAAAAAAAAlQL5AA=",
                operation.toXdrBase64());
    }

    @Test
    public void testPathPaymentOperation() throws FormatException, IOException, AssetCodeLengthInvalidException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair destination = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");
        // GCGZLB3X2B3UFOFSHHQ6ZGEPEX7XYPEH6SBFMIV74EUDOFZJA3VNL6X4
        KeyPair issuer = KeyPair.fromSecretSeed("SBOBVZUN6WKVMI6KIL2GHBBEETEV6XKQGILITNH6LO6ZA22DBMSDCPAG");

        // GAVAQKT2M7B4V3NN7RNNXPU5CWNDKC27MYHKLF5UNYXH4FNLFVDXKRSV
        KeyPair pathIssuer1 = KeyPair.fromSecretSeed("SALDLG5XU5AEJWUOHAJPSC4HJ2IK3Z6BXXP4GWRHFT7P7ILSCFFQ7TC5");
        // GBCP5W2VS7AEWV2HFRN7YYC623LTSV7VSTGIHFXDEJU7S5BAGVCSETRR
        KeyPair pathIssuer2 = KeyPair.fromSecretSeed("SA64U7C5C7BS5IHWEPA7YWFN3Z6FE5L6KAMYUIT4AQ7KVTVLD23C6HEZ");

        Asset sendAsset = new AssetTypeNative();
        String sendMax = "0.0001";
        Asset destAsset = new AssetTypeCreditAlphaNum4("USD", issuer);
        String destAmount = "0.0001";
        Asset[] path = {new AssetTypeCreditAlphaNum4("USD", pathIssuer1), new AssetTypeCreditAlphaNum12("TESTTEST", pathIssuer2)};

        PathPaymentOperation operation = new PathPaymentOperation.Builder(
                sendAsset, sendMax, destination, destAsset, destAmount)
                .setPath(path)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        PathPaymentOperation parsedOperation = (PathPaymentOperation) Operation.fromXdr(xdr);

        assertEquals(1000L, xdr.getBody().getPathPaymentOp().getSendMax().getInt64().longValue());
        assertEquals(1000L, xdr.getBody().getPathPaymentOp().getDestAmount().getInt64().longValue());
        assertTrue(parsedOperation.getSendAsset() instanceof AssetTypeNative);
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());
        assertEquals(destination.getAccountId(), parsedOperation.getDestination().getAccountId());
        assertEquals(sendMax, parsedOperation.getSendMax());
        assertTrue(parsedOperation.getDestAsset() instanceof AssetTypeCreditAlphaNum4);
        assertEquals(destAmount, parsedOperation.getDestAmount());
        assertEquals(path.length, parsedOperation.getPath().length);

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAIAAAAAAAAAAAAAA+gAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAABVVNEAAAAAACNlYd30HdCuLI54eyYjyX/fDyH9IJWIr/hKDcXKQbq1QAAAAAAAAPoAAAAAgAAAAFVU0QAAAAAACoIKnpnw8rtrfxa276dFZo1C19mDqWXtG4ufhWrLUd1AAAAAlRFU1RURVNUAAAAAAAAAABE/ttVl8BLV0csW/xgXtbXOVf1lMyDluMiafl0IDVFIg==",
                operation.toXdrBase64());
    }

    @Test
    public void testPathPaymentEmptyPathOperation() throws FormatException, IOException, AssetCodeLengthInvalidException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair destination = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");
        // GCGZLB3X2B3UFOFSHHQ6ZGEPEX7XYPEH6SBFMIV74EUDOFZJA3VNL6X4
        KeyPair issuer = KeyPair.fromSecretSeed("SBOBVZUN6WKVMI6KIL2GHBBEETEV6XKQGILITNH6LO6ZA22DBMSDCPAG");

        // GAVAQKT2M7B4V3NN7RNNXPU5CWNDKC27MYHKLF5UNYXH4FNLFVDXKRSV
        KeyPair pathIssuer1 = KeyPair.fromSecretSeed("SALDLG5XU5AEJWUOHAJPSC4HJ2IK3Z6BXXP4GWRHFT7P7ILSCFFQ7TC5");
        // GBCP5W2VS7AEWV2HFRN7YYC623LTSV7VSTGIHFXDEJU7S5BAGVCSETRR
        KeyPair pathIssuer2 = KeyPair.fromSecretSeed("SA64U7C5C7BS5IHWEPA7YWFN3Z6FE5L6KAMYUIT4AQ7KVTVLD23C6HEZ");

        Asset sendAsset = new AssetTypeNative();
        String sendMax = "0.0001";
        Asset destAsset = new AssetTypeCreditAlphaNum4("USD", issuer);
        String destAmount = "0.0001";

        PathPaymentOperation operation = new PathPaymentOperation.Builder(
                sendAsset, sendMax, destination, destAsset, destAmount)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        PathPaymentOperation parsedOperation = (PathPaymentOperation) Operation.fromXdr(xdr);

        assertEquals(1000L, xdr.getBody().getPathPaymentOp().getSendMax().getInt64().longValue());
        assertEquals(1000L, xdr.getBody().getPathPaymentOp().getDestAmount().getInt64().longValue());
        assertTrue(parsedOperation.getSendAsset() instanceof AssetTypeNative);
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());
        assertEquals(destination.getAccountId(), parsedOperation.getDestination().getAccountId());
        assertEquals(sendMax, parsedOperation.getSendMax());
        assertTrue(parsedOperation.getDestAsset() instanceof AssetTypeCreditAlphaNum4);
        assertEquals(destAmount, parsedOperation.getDestAmount());
        assertEquals(0, parsedOperation.getPath().length);

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAIAAAAAAAAAAAAAA+gAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAABVVNEAAAAAACNlYd30HdCuLI54eyYjyX/fDyH9IJWIr/hKDcXKQbq1QAAAAAAAAPoAAAAAA==",
                operation.toXdrBase64());
    }

    @Test
    public void testChangeTrustOperation() throws FormatException, IOException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");

        Asset asset = new AssetTypeNative();
        String limit = "922337203685.4775807";

        ChangeTrustOperation operation = new ChangeTrustOperation.Builder(asset, limit)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        ChangeTrustOperation parsedOperation = (ChangeTrustOperation) Operation.fromXdr(xdr);

        assertEquals(9223372036854775807L, xdr.getBody().getChangeTrustOp().getLimit().getInt64().longValue());
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());
        assertTrue(parsedOperation.getAsset() instanceof AssetTypeNative);
        assertEquals(limit, parsedOperation.getLimit());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAYAAAAAf/////////8=",
                operation.toXdrBase64());
    }

    @Test
    public void testAllowTrustOperation() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair trustor = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");

        String assetCode = "USDA";
        boolean authorize = true;

        AllowTrustOperation operation = new AllowTrustOperation.Builder(trustor, assetCode, authorize)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        AllowTrustOperation parsedOperation = (AllowTrustOperation) Operation.fromXdr(xdr);

        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());
        assertEquals(trustor.getAccountId(), parsedOperation.getTrustor().getAccountId());
        assertEquals(assetCode, parsedOperation.getAssetCode());
        assertEquals(authorize, parsedOperation.getAuthorize());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAcAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAABVVNEQQAAAAE=",
                operation.toXdrBase64());
    }

    @Test
    public void testAllowTrustOperationAssetCodeBuffer() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair trustor = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");

        String assetCode = "USDABC";
        boolean authorize = true;

        AllowTrustOperation operation = new AllowTrustOperation.Builder(trustor, assetCode, authorize)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        AllowTrustOperation parsedOperation = (AllowTrustOperation) Operation.fromXdr(xdr);

        assertEquals(assetCode, parsedOperation.getAssetCode());
    }

    @Test
    public void testSetOptionsOperation() throws FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair inflationDestination = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");
        // GBCP5W2VS7AEWV2HFRN7YYC623LTSV7VSTGIHFXDEJU7S5BAGVCSETRR
        SignerKey signer = Signer.ed25519PublicKey(KeyPair.fromSecretSeed("SA64U7C5C7BS5IHWEPA7YWFN3Z6FE5L6KAMYUIT4AQ7KVTVLD23C6HEZ"));

        Integer clearFlags = 1;
        Integer setFlags = 1;
        Integer masterKeyWeight = 1;
        Integer lowThreshold = 2;
        Integer mediumThreshold = 3;
        Integer highThreshold = 4;
        String homeDomain = "stellar.org";
        Integer signerWeight = 1;

        SetOptionsOperation operation = new SetOptionsOperation.Builder()
                .setInflationDestination(inflationDestination)
                .setClearFlags(clearFlags)
                .setSetFlags(setFlags)
                .setMasterKeyWeight(masterKeyWeight)
                .setLowThreshold(lowThreshold)
                .setMediumThreshold(mediumThreshold)
                .setHighThreshold(highThreshold)
                .setHomeDomain(homeDomain)
                .setSigner(signer, signerWeight)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        SetOptionsOperation parsedOperation = (SetOptionsOperation) SetOptionsOperation.fromXdr(xdr);

        assertEquals(inflationDestination.getAccountId(), parsedOperation.getInflationDestination().getAccountId());
        assertEquals(clearFlags, parsedOperation.getClearFlags());
        assertEquals(setFlags, parsedOperation.getSetFlags());
        assertEquals(masterKeyWeight, parsedOperation.getMasterKeyWeight());
        assertEquals(lowThreshold, parsedOperation.getLowThreshold());
        assertEquals(mediumThreshold, parsedOperation.getMediumThreshold());
        assertEquals(highThreshold, parsedOperation.getHighThreshold());
        assertEquals(homeDomain, parsedOperation.getHomeDomain());
        assertEquals(signer.getDiscriminant().getValue(), parsedOperation.getSigner().getDiscriminant().getValue());
        assertEquals(signer.getEd25519().getUint256(), parsedOperation.getSigner().getEd25519().getUint256());
        assertEquals(signerWeight, parsedOperation.getSignerWeight());
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAUAAAABAAAAAO3gUmG83C+VCqO6FztuMtXJF/l7grZA7MjRzqdZ9W8QAAAAAQAAAAEAAAABAAAAAQAAAAEAAAABAAAAAQAAAAIAAAABAAAAAwAAAAEAAAAEAAAAAQAAAAtzdGVsbGFyLm9yZwAAAAABAAAAAET+21WXwEtXRyxb/GBe1tc5V/WUzIOW4yJp+XQgNUUiAAAAAQ==",
                operation.toXdrBase64());
    }

    @Test
    public void testSetOptionsOperationSingleField() {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");

        String homeDomain = "stellar.org";

        SetOptionsOperation operation = new SetOptionsOperation.Builder()
                .setHomeDomain(homeDomain)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        SetOptionsOperation parsedOperation = (SetOptionsOperation) SetOptionsOperation.fromXdr(xdr);

        assertEquals(null, parsedOperation.getInflationDestination());
        assertEquals(null, parsedOperation.getClearFlags());
        assertEquals(null, parsedOperation.getSetFlags());
        assertEquals(null, parsedOperation.getMasterKeyWeight());
        assertEquals(null, parsedOperation.getLowThreshold());
        assertEquals(null, parsedOperation.getMediumThreshold());
        assertEquals(null, parsedOperation.getHighThreshold());
        assertEquals(homeDomain, parsedOperation.getHomeDomain());
        assertEquals(null, parsedOperation.getSigner());
        assertEquals(null, parsedOperation.getSignerWeight());
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAtzdGVsbGFyLm9yZwAAAAAA",
                operation.toXdrBase64());
    }

    @Test
    public void testSetOptionsOperationSignerSha256() {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");

        byte[] preimage = "stellar.org".getBytes();
        byte[] hash = Util.hash(preimage);

        SetOptionsOperation operation = new SetOptionsOperation.Builder()
                .setSigner(Signer.sha256Hash(hash), 10)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        SetOptionsOperation parsedOperation = (SetOptionsOperation) SetOptionsOperation.fromXdr(xdr);

        assertEquals(null, parsedOperation.getInflationDestination());
        assertEquals(null, parsedOperation.getClearFlags());
        assertEquals(null, parsedOperation.getSetFlags());
        assertEquals(null, parsedOperation.getMasterKeyWeight());
        assertEquals(null, parsedOperation.getLowThreshold());
        assertEquals(null, parsedOperation.getMediumThreshold());
        assertEquals(null, parsedOperation.getHighThreshold());
        assertEquals(null, parsedOperation.getHomeDomain());
        assertTrue(Arrays.equals(hash, parsedOperation.getSigner().getHashX().getUint256()));
        assertEquals(new Integer(10), parsedOperation.getSignerWeight());
        assertEquals(source.getAccountId(), parsedOperation.getSourceAccount().getAccountId());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAACbpRqMkaQAfCYSk/n3xIl4fCoHfKqxF34ht2iuvSYEJQAAAAK",
                operation.toXdrBase64());
    }

    @Test
    public void testSetOptionsOperationPreAuthTxSigner() {
        Network.useTestNetwork();

        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .build();

        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair opSource = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");

        SetOptionsOperation operation = new SetOptionsOperation.Builder()
                .setSigner(Signer.preAuthTx(transaction), 10)
                .setSourceAccount(opSource)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        SetOptionsOperation parsedOperation = (SetOptionsOperation) SetOptionsOperation.fromXdr(xdr);

        assertEquals(null, parsedOperation.getInflationDestination());
        assertEquals(null, parsedOperation.getClearFlags());
        assertEquals(null, parsedOperation.getSetFlags());
        assertEquals(null, parsedOperation.getMasterKeyWeight());
        assertEquals(null, parsedOperation.getLowThreshold());
        assertEquals(null, parsedOperation.getMediumThreshold());
        assertEquals(null, parsedOperation.getHighThreshold());
        assertEquals(null, parsedOperation.getHomeDomain());
        assertTrue(Arrays.equals(transaction.hash(), parsedOperation.getSigner().getPreAuthTx().getUint256()));
        assertEquals(new Integer(10), parsedOperation.getSignerWeight());
        assertEquals(opSource.getAccountId(), parsedOperation.getSourceAccount().getAccountId());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAB1vRBIRC3w7ZH5rQa17hIBKUwZTvBP4kNmSP7jVyw1fQAAAAK",
                operation.toXdrBase64());
    }

    @Test
    public void testManageOfferOperation() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GBCP5W2VS7AEWV2HFRN7YYC623LTSV7VSTGIHFXDEJU7S5BAGVCSETRR
        KeyPair issuer = KeyPair.fromSecretSeed("SA64U7C5C7BS5IHWEPA7YWFN3Z6FE5L6KAMYUIT4AQ7KVTVLD23C6HEZ");

        Asset selling = new AssetTypeNative();
        Asset buying = Asset.createNonNativeAsset("USD", issuer);
        String amount = "0.00001";
        String price = "0.85334384"; // n=5333399 d=6250000
        Price priceObj = Price.fromString(price);
        long offerId = 1;

        ManageOfferOperation operation = new ManageOfferOperation.Builder(selling, buying, amount, price)
                .setOfferId(offerId)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        ManageOfferOperation parsedOperation = (ManageOfferOperation) ManageOfferOperation.fromXdr(xdr);

        assertEquals(100L, xdr.getBody().getManageOfferOp().getAmount().getInt64().longValue());
        assertTrue(parsedOperation.getSelling() instanceof AssetTypeNative);
        assertTrue(parsedOperation.getBuying() instanceof AssetTypeCreditAlphaNum4);
        assertTrue(parsedOperation.getBuying().equals(buying));
        assertEquals(amount, parsedOperation.getAmount());
        assertEquals(price, parsedOperation.getPrice());
        assertEquals(priceObj.getNumerator(), 5333399);
        assertEquals(priceObj.getDenominator(), 6250000);
        assertEquals(offerId, parsedOperation.getOfferId());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAMAAAAAAAAAAVVTRAAAAAAARP7bVZfAS1dHLFv8YF7W1zlX9ZTMg5bjImn5dCA1RSIAAAAAAAAAZABRYZcAX14QAAAAAAAAAAE=",
                operation.toXdrBase64());
    }

    @Test
    public void testCreatePassiveOfferOperation() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GBCP5W2VS7AEWV2HFRN7YYC623LTSV7VSTGIHFXDEJU7S5BAGVCSETRR
        KeyPair issuer = KeyPair.fromSecretSeed("SA64U7C5C7BS5IHWEPA7YWFN3Z6FE5L6KAMYUIT4AQ7KVTVLD23C6HEZ");

        Asset selling = new AssetTypeNative();
        Asset buying = Asset.createNonNativeAsset("USD", issuer);
        String amount = "0.00001";
        String price = "2.93850088"; // n=36731261 d=12500000
        Price priceObj = Price.fromString(price);

        CreatePassiveOfferOperation operation = new CreatePassiveOfferOperation.Builder(selling, buying, amount, price)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();
        CreatePassiveOfferOperation parsedOperation = (CreatePassiveOfferOperation) CreatePassiveOfferOperation.fromXdr(xdr);

        assertEquals(100L, xdr.getBody().getCreatePassiveOfferOp().getAmount().getInt64().longValue());
        assertTrue(parsedOperation.getSelling() instanceof AssetTypeNative);
        assertTrue(parsedOperation.getBuying() instanceof AssetTypeCreditAlphaNum4);
        assertTrue(parsedOperation.getBuying().equals(buying));
        assertEquals(amount, parsedOperation.getAmount());
        assertEquals(price, parsedOperation.getPrice());
        assertEquals(priceObj.getNumerator(), 36731261);
        assertEquals(priceObj.getDenominator(), 12500000);

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAQAAAAAAAAAAVVTRAAAAAAARP7bVZfAS1dHLFv8YF7W1zlX9ZTMg5bjImn5dCA1RSIAAAAAAAAAZAIweX0Avrwg",
                operation.toXdrBase64());
    }

    @Test
    public void testAccountMergeOperation() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");
        // GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR
        KeyPair destination = KeyPair.fromSecretSeed("SDHZGHURAYXKU2KMVHPOXI6JG2Q4BSQUQCEOY72O3QQTCLR2T455PMII");

        AccountMergeOperation operation = new AccountMergeOperation.Builder(destination)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();

        AccountMergeOperation parsedOperation = (AccountMergeOperation) Operation.fromXdr(xdr);

        assertEquals(destination.getAccountId(), parsedOperation.getDestination().getAccountId());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAgAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxA=",
                operation.toXdrBase64());
    }

    @Test
    public void testManageDataOperation() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");

        ManageDataOperation operation = new ManageDataOperation.Builder("test", new byte[]{0, 1, 2, 3, 4})
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();

        ManageDataOperation parsedOperation = (ManageDataOperation) Operation.fromXdr(xdr);

        assertEquals("test", parsedOperation.getName());
        assertTrue(Arrays.equals(new byte[]{0, 1, 2, 3, 4}, parsedOperation.getValue()));

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAoAAAAEdGVzdAAAAAEAAAAFAAECAwQAAAA=",
                operation.toXdrBase64());
    }

    @Test
    public void testManageDataOperationEmptyValue() throws IOException, FormatException {
        // GC5SIC4E3V56VOHJ3OZAX5SJDTWY52JYI2AFK6PUGSXFVRJQYQXXZBZF
        KeyPair source = KeyPair.fromSecretSeed("SC4CGETADVYTCR5HEAVZRB3DZQY5Y4J7RFNJTRA6ESMHIPEZUSTE2QDK");

        ManageDataOperation operation = new ManageDataOperation.Builder("test", null)
                .setSourceAccount(source)
                .build();

        org.stellar.sdk.xdr.Operation xdr = operation.toXdr();

        ManageDataOperation parsedOperation = (ManageDataOperation) Operation.fromXdr(xdr);

        assertEquals("test", parsedOperation.getName());
        assertEquals(null, parsedOperation.getValue());

        assertEquals(
                "AAAAAQAAAAC7JAuE3XvquOnbsgv2SRztjuk4RoBVefQ0rlrFMMQvfAAAAAoAAAAEdGVzdAAAAAA=",
                operation.toXdrBase64());
    }

    @Test
    public void testToXdrAmount() {
        assertEquals(0L, Operation.toXdrAmount("0"));
        assertEquals(1L, Operation.toXdrAmount("0.0000001"));
        assertEquals(10000000L, Operation.toXdrAmount("1"));
        assertEquals(11234567L, Operation.toXdrAmount("1.1234567"));
        assertEquals(729912843007381L, Operation.toXdrAmount("72991284.3007381"));
        assertEquals(729912843007381L, Operation.toXdrAmount("72991284.30073810"));
        assertEquals(1014016711446800155L, Operation.toXdrAmount("101401671144.6800155"));
        assertEquals(9223372036854775807L, Operation.toXdrAmount("922337203685.4775807"));

        try {
            Operation.toXdrAmount("0.00000001");
            fail();
        }
        catch (ArithmeticException e) {}
        catch (Exception e) { fail(); }

        try {
            Operation.toXdrAmount("72991284.30073811");
            fail();
        }
        catch (ArithmeticException e) {}
        catch (Exception e) { fail(); }
    }

    @Test
    public void testFromXdrAmount() {
        assertEquals("0", Operation.fromXdrAmount(0L));
        assertEquals("0.0000001", Operation.fromXdrAmount(1L));
        assertEquals("1", Operation.fromXdrAmount(10000000L));
        assertEquals("1.1234567", Operation.fromXdrAmount(11234567L));
        assertEquals("72991284.3007381", Operation.fromXdrAmount(729912843007381L));
        assertEquals("101401671144.6800155", Operation.fromXdrAmount(1014016711446800155L));
        assertEquals("922337203685.4775807", Operation.fromXdrAmount(9223372036854775807L));
    }*/
}
