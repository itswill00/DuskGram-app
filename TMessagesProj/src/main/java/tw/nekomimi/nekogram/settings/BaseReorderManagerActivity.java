package tw.nekomimi.nekogram.settings;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;

/**
 * Shared base for settings screens built around drag-to-reorder sections.
 *
 * It bundles the pieces those screens all need: the ItemTouchHelper that drives
 * dragging, the trailing grab handle, and a reset action in the action bar. Each
 * screen only supplies the data behaviour through {@link ReorderDelegate}.
 */
public abstract class BaseReorderManagerActivity extends BaseNekoSettingsActivity {

    private Drawable reorderIcon;

    /** Supplies the data-specific behaviour for {@link #attachReorder}. */
    protected interface ReorderDelegate {
        /** Whether the row at {@code position} can be dragged at all. */
        boolean isDraggable(int position);

        /** Whether {@code from} and {@code to} belong to the same reorderable section. */
        boolean isSameSection(int from, int to);

        /** Apply the move for a drag from {@code from} to {@code to}. */
        void onMove(int from, int to);

        /** Persist the new order and refresh dependent UI once a drag finishes. */
        void onReorderFinished();
    }

    /** Attaches drag-to-reorder (within sections) to {@link #listView}. */
    protected final void attachReorder(ReorderDelegate delegate) {
        ItemTouchHelper helper = new ItemTouchHelper(new SectionReorderCallback(this, delegate));
        helper.attachToRecyclerView(listView);
    }

    /** Draws the trailing grab handle on a {@link TextCell} row. */
    protected void applyReorderHandle(TextCell cell) {
        if (reorderIcon == null) {
            reorderIcon = ContextCompat.getDrawable(getContext(), R.drawable.list_reorder);
        }
        ImageView iv = cell.getValueImageView();
        if (iv != null && reorderIcon != null) {
            iv.setVisibility(View.VISIBLE);
            iv.setImageDrawable(reorderIcon);
            iv.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
        }
    }

    /** Adds a reset-to-default button to the action bar. */
    protected ActionBarMenuItem addResetMenuItem(int menuId) {
        ActionBarMenuItem item = actionBar.createMenu().addItem(menuId, R.drawable.msg_reset);
        item.setContentDescription(LocaleController.getString(R.string.Reset));
        return item;
    }

    /** Animated show/hide of the reset button depending on whether the layout is default. */
    protected void updateResetButtonVisibility(ActionBarMenuItem item, boolean isDefault) {
        if (item == null) {
            return;
        }
        if (!isDefault && item.getVisibility() == View.GONE) {
            AndroidUtilities.updateViewVisibilityAnimated(item, true, 0.5f, true);
        } else if (isDefault && item.getVisibility() == View.VISIBLE) {
            AndroidUtilities.updateViewVisibilityAnimated(item, false, 0.5f, true);
        }
    }

    private static class SectionReorderCallback extends ItemTouchHelper.Callback {
        private final ReorderDelegate delegate;
        private final BaseReorderManagerActivity host;

        SectionReorderCallback(BaseReorderManagerActivity host, ReorderDelegate delegate) {
            this.host = host;
            this.delegate = delegate;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getAdapterPosition();
            return delegate.isDraggable(position)
                    ? makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0)
                    : 0;
        }

        @Override
        public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
            return delegate.isSameSection(current.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            if (delegate.isSameSection(from, to)) {
                delegate.onMove(from, to);
                return true;
            }
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE && viewHolder != null) {
                host.listView.hideSelector(false);
                viewHolder.itemView.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(16), host.getThemedColor(Theme.key_windowBackgroundWhite)));
                viewHolder.itemView.setPressed(true);
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
            viewHolder.itemView.setBackground(null);
            host.listView.hideSelector(false);
            delegate.onReorderFinished();
        }
    }
}
