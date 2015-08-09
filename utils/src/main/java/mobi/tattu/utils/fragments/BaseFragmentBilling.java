package mobi.tattu.utils.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import java.util.UUID;

import mobi.tattu.utils.activities.BaseActivity;
import mobi.tattu.utils.annotations.Nullable;
import mobi.tattu.utils.billing.IabHelper;
import mobi.tattu.utils.billing.IabResult;
import mobi.tattu.utils.billing.Inventory;
import mobi.tattu.utils.billing.Purchase;

/**
 * Created by cristian on 04/08/15.
 * // TODO: 07/08/15 Hay que hacer esta seccion de nuevo, pq se esta validando mal cuando se compro un item 
 * // TODO: 07/08/15 en esta clase se tiene que iniciar el servicio , para mas ejemplos mirar el proyecto TrivialDrive 
 */
public abstract class BaseFragmentBilling extends BaseFragment implements IabHelper.OnIabPurchaseFinishedListener,
        IabHelper.QueryInventoryFinishedListener, IabHelper.OnConsumeFinishedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(getIabHelper() != null && Utils.checkConnection(getActivity())){
//            consumeItem();
//        }
    }


    public String ITEM_SKU_TEST_GOOGLE = "android.test.purchased";

    /**
     * @return sku para indicarle a google el item que se quiere comprar
     */
    public abstract String getSku();

    /**
     * @return indica un numero de request para manejar en la actividad, si es necesario hacer algo antes de enviar la informacion para el pago
     */
    public abstract int getRequestCode();

    /**
     * Este metodo se invoca cuando se lanza la opcion para comprar un item, se
     * puede usar para cambiar algun estado en la UI
     */
    public abstract void launcherPurchaseListener();

    /**
     * Indica que hubo un error al lanzar el pago
     */
    public abstract void onIabPurchaseFinishedFailure(Purchase purchase, IabResult result);

    /**
     * Se invoca cuando fallo al comprobar el pago
     *
     * @param result
     */
    public abstract void onQueryInventoryFinishedFailure(Inventory inventory, IabResult result);

    public abstract void onConsumeFinishSucces(Purchase purchase, IabResult result);

    public abstract void onConsumeFinishFailure(Purchase purchase, IabResult result);

    public abstract IabHelper getIabHelper();

    /**
     * Indica que la copia que se esta usando pirata trucha
     *
     */
    public abstract void isCopyPirate();


    /**
     *
     * @return un fragment para mostrar cuando se realizo la compra con exito. este valor puede ser null
     */
    public abstract @Nullable Fragment launcherFragmentSuccessBilling();

    /**
     * Lanza la ventana para comprar un item
     */
    public void launcherPurchase() {
        launcherPurchaseListener();
        getIabHelper().launchPurchaseFlow(this.getBaseActivity(), getSku(), getRequestCode(),
                this, UUID.randomUUID().toString());
    }


    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

        if (result.isFailure()) {
            onIabPurchaseFinishedFailure(purchase, result);
            return;
        } else if (purchase != null && purchase.getSku().equals(getSku())) {
            consumeItem();

        }
    }

    public void consumeItem() {
        getIabHelper().queryInventoryAsync(this);
    }


    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (result.isFailure()) {
            if(result.getMessage().contains("1003:Purchase signature verification failed")){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Copia Pirata");
                builder.setMessage("Esta usando una copia pirata,esta version no es original y pueden estar robando sus datos personales.Para validar la aplicacion y remover las partes no originales" +
                        "Aprete el boton comprar");
                builder.setPositiveButton("Comprar",(dialog, which) -> {isCopyPirate();launcherPurchase();});
            }else{
                onQueryInventoryFinishedFailure(inventory, result);
            }
        } else {
            showLoading();
            getIabHelper().consumeAsync(inventory.getPurchase(getSku()),
                    this);
        }
    }

    @Override
    public void onConsumeFinished(Purchase purchase, IabResult result) {
        if (result.isSuccess()) {

            onConsumeFinishSucces(purchase, result);
            stopLoading();
            if(launcherFragmentSuccessBilling() != null){
                ((BaseActivity) getActivity()).start(launcherFragmentSuccessBilling(),false);
            }
        } else {
            onConsumeFinishFailure(purchase, result);
        }
    }


}
