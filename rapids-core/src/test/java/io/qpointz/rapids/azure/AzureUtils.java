package io.qpointz.rapids.azure;

public class AzureUtils {

    public static String storageAccountName = System.getenv("RAPIDS_IT_AZURE_STORAGE_ACCOUNT_NAME");
    public static String storageAccountKey = System.getenv("RAPIDS_IT_AZURE_STORAGE_ACCOUNT_KEY");

    public static String itContainer = "rapids-it";
    public static String itContainerModels = "rapids-it-models";
}
