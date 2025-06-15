package io.github.anjoismysign.ui;

import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;

public class BlobTycoonUI {
    private static BlobTycoonUI instance;
    private final PlotHelperUI plotHelperUI;
    private final RebirthUI rebirthUI;
    private final ProfileUI profileUI;
    private final ActionHolderUI actionHolderUI;

    public static BlobTycoonUI getInstance() {
        if (instance == null) {
            instance = new BlobTycoonUI();
        }
        return instance;
    }
    private BlobTycoonUI() {
        plotHelperUI = new PlotHelperUI();
        rebirthUI = new RebirthUI();
        profileUI = new ProfileUI();
        actionHolderUI = new ActionHolderUI();
    }

    public void reload() {
        BlobLibInventoryAPI inventoryAPI = BlobLibInventoryAPI.getInstance();
        plotHelperUI.reload(inventoryAPI);
        rebirthUI.reload(inventoryAPI);
        profileUI.reload(inventoryAPI);
        actionHolderUI.reload(inventoryAPI);
    }
}
