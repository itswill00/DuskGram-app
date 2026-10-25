package tw.nekomimi.nekogram.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextCell;

import java.util.ArrayList;
import java.util.List;

import tw.nekomimi.nekogram.NekoConfig;

/**
 * Lets the user drag the configurable drawer rows into their preferred order.
 *
 * Only the order lives here; visibility is still handled by each row's own
 * switch, so dragging can never hide anything.
 */
public class DrawerMenuActivity extends BaseReorderManagerActivity {

    private static final int MENU_RESET = 1;

    private final List<String> order = new ArrayList<>();

    private int headerRow;
    private int orderStartRow;
    private int orderEndRow;
    private int infoRow;

    private ActionBarMenuItem resetItem;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.DrawerOrderTitle);
    }

    @Override
    protected String getKey() {
        return "drawer";
    }

    @Override
    public View createView(Context context) {
        updateRows();
        View view = super.createView(context);

        resetItem = addResetMenuItem(MENU_RESET);
        resetItem.setOnClickListener(v -> {
            NekoConfig.DrawerOrder.reset();
            reload();
            updateRows();
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
            updateResetVisibility();
            notifyDrawerChanged();
        });
        updateResetVisibility();

        attachReorder(new ReorderDelegate() {
            @Override
            public boolean isDraggable(int position) {
                return position >= orderStartRow && position <= orderEndRow;
            }

            @Override
            public boolean isSameSection(int from, int to) {
                return isDraggable(from) && isDraggable(to);
            }

            @Override
            public void onMove(int from, int to) {
                int a = from - orderStartRow;
                int b = to - orderStartRow;
                if (a < 0 || b < 0 || a >= order.size() || b >= order.size()) {
                    return;
                }
                order.add(b, order.remove(a));
                if (listAdapter != null) {
                    listAdapter.notifyItemMoved(from, to);
                }
            }

            @Override
            public void onReorderFinished() {
                NekoConfig.DrawerOrder.save(order);
                updateResetVisibility();
                notifyDrawerChanged();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
            }
        });

        return view;
    }

    private void reload() {
        order.clear();
        order.addAll(NekoConfig.DrawerOrder.current());
    }

    private void updateResetVisibility() {
        updateResetButtonVisibility(resetItem, NekoConfig.DrawerOrder.isDefault());
    }

    private void notifyDrawerChanged() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
    }

    /** Human readable label for a stored drawer row id. */
    private String labelFor(String id) {
        switch (id) {
            case "recent":
                return LocaleController.getString(R.string.RecentChats);
            case "newgroup":
                return LocaleController.getString(R.string.NewGroup);
            case "newmessage":
                return LocaleController.getString(R.string.NewMessageTitle);
            case "saved":
                return LocaleController.getString(R.string.SavedMessages);
            case "ghost":
                return LocaleController.getString(R.string.GhostMode);
            case "profile":
                return LocaleController.getString(R.string.MyProfile);
            default:
                return id;
        }
    }

    @Override
    protected void updateRows() {
        super.updateRows();
        reload();

        headerRow = addRow("drawerHeader");
        orderStartRow = rowCount;
        for (String id : order) {
            addRow("drawer_" + id);
        }
        orderEndRow = rowCount - 1;
        infoRow = addRow("drawerInfo");
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        // Rows are reordered by dragging; tapping does nothing.
    }

    private class ListAdapter extends BaseListAdapter {

        ListAdapter(Context context) {
            super(context);
        }

        @SuppressLint("NewApi")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial, boolean divider) {
            switch (holder.getItemViewType()) {
                case TYPE_HEADER: {
                    ((HeaderCell) holder.itemView).setText(LocaleController.getString(R.string.DrawerOrderTitle));
                    break;
                }
                case TYPE_TEXT: {
                    TextCell cell = (TextCell) holder.itemView;
                    int index = position - orderStartRow;
                    String id = index >= 0 && index < order.size() ? order.get(index) : "";
                    cell.setTextAndIcon(labelFor(id), R.drawable.list_reorder, false);
                    applyReorderHandle(cell);
                    break;
                }
                case TYPE_INFO_PRIVACY: {
                    ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.DrawerOrderInfo));
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == headerRow) {
                return TYPE_HEADER;
            }
            if (position >= orderStartRow && position <= orderEndRow) {
                return TYPE_TEXT;
            }
            if (position == infoRow) {
                return TYPE_INFO_PRIVACY;
            }
            return super.getItemViewType(position);
        }
    }
}
