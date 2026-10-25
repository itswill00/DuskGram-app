package tw.nekomimi.nekogram.config;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

import tw.nekomimi.nekogram.config.cell.AbstractConfigCell;
import tw.nekomimi.nekogram.config.cell.ConfigCellDivider;

public class CellGroup {
    public static final int ITEM_TYPE_DIVIDER = 1;
    public static final int ITEM_TYPE_TEXT_SETTINGS_CELL = 2;
    public static final int ITEM_TYPE_TEXT_CHECK = 3;
    public static final int ITEM_TYPE_HEADER = 4;
    public static final int ITEM_TYPE_TEXT_DETAIL = 5;
    public static final int ITEM_TYPE_TEXT = 6;

    public BaseFragment thisFragment;
    public RecyclerListView listView;
    public RecyclerListView.SelectionAdapter listAdapter;
    public ArrayList<AbstractConfigCell> rows;

    public CallBackSettingsChanged callBackSettingsChanged;

    public CellGroup(BaseFragment thisFragment) {
        this.thisFragment = thisFragment;
        this.rows = new ArrayList<>();
    }

    public void setListAdapter(RecyclerListView lv, RecyclerListView.SelectionAdapter la) {
        this.listView = lv;
        this.listAdapter = la;
    }

    public AbstractConfigCell appendCell(AbstractConfigCell cell) {
        cell.bindCellGroup(this);
        this.rows.add(cell);
        return cell;
    }

    public AbstractConfigCell appendCell(AbstractConfigCell cell, boolean display) {
        cell.bindCellGroup(this);
        if (display) // For censored features, don't show it forever.
            this.rows.add(cell);
        return cell;
    }

    public interface CallBackSettingsChanged {
        void run(String key, Object newValue);
    }

    public void runCallback(String key, Object newValue) {
        if (callBackSettingsChanged == null) return;
        try {
            callBackSettingsChanged.run(key, newValue);
        } catch (Exception e) {

        }
    }

    public interface OnBindViewHolder {
        void onBindViewHolder(RecyclerView.ViewHolder holder);
    }

    //Utils
    public static void hideItemFromRecyclerView(View cell, boolean hide) {
        if (cell == null) return;
        if (cell != null) return; //TODO hideItemFromRecyclerView
        ViewGroup.LayoutParams params = cell.getLayoutParams();
        if (hide) {
            cell.setVisibility(View.GONE);
            params.height = 0;
        } else {
            cell.setVisibility(View.VISIBLE);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        cell.setLayoutParams(params);
    }

    public boolean needSetDivider(AbstractConfigCell cell) {
        int index = rows.indexOf(cell);
        if (index < 0) {
            return true;
        }
        int next = index + 1;
        // The last row has nothing below it, so it draws no divider. Without
        // this bound the lookup throws and RecyclerView keeps retrying the bind,
        // which turns into an ANR rather than a normal crash.
        if (next >= rows.size()) {
            return false;
        }
        return !(rows.get(next) instanceof ConfigCellDivider);
    }

}