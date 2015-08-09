package mobi.tattu.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Leandro on 7/8/2015.
 */
public class DialogUtils {

    /**
     * Muestra un dialog con radio boxes con estilo Material Design.
     *
     * @param ctx      Activity
     * @param items    Array de los items. Los items deben implementar el metodo toString
     * @param selected El item seleccionado por defecto o null si no hay valor preseleccionado
     * @param title    El titulo del dialogo
     * @param listener Listener para la seleccion o null. El listener debe devolver true si es
     *                 necesario cerrar el dialogo luego de la selección. El lister recibe
     *                 el elemento seleccionado y el objeto AlertDialog
     * @param <T>      El tipo de item. El metodo toString debe devolver el nombre del item que va a
     *                 aparecer en la UI.
     */
    public static <T> AlertDialog showSingleChoiceDialog(Context ctx, T[] items, T selected, String title,
                                                         F.Function2<T, AlertDialog, Boolean> listener) {
        ArrayAdapter<T> adapter = new ArrayAdapter(ctx,
                R.layout.select_dialog_singlechoice_material,
                items);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setAdapter(adapter, (d, which) -> {
                })
                .create();

        dialog.getListView().setItemsCanFocus(false);
        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null && listener.apply(adapter.getItem(position), dialog)) {
                dialog.dismiss();
            }
        });
        dialog.show();

        if (selected != null) {
            dialog.getListView().setItemChecked(adapter.getPosition(selected), true);
        }

        return dialog;
    }

}
