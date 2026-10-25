package tw.nekomimi.nekogram.settings;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.config.CellGroup;
import tw.nekomimi.nekogram.config.cell.AbstractConfigCell;
import tw.nekomimi.nekogram.config.cell.ConfigCellDivider;
import tw.nekomimi.nekogram.config.cell.ConfigCellHeader;
import tw.nekomimi.nekogram.config.cell.ConfigCellText;
import tw.nekomimi.nekogram.config.cell.ConfigCellTextCheck;
import xyz.nextalone.nagram.NaConfig;

/**
 * Hub for the activity-hiding and content-control features, mirroring the
 * AyuMoments page in Nagram XF.
 *
 * Every entry already existed as its own screen or switch; this page is the
 * single entry point that ties them together.
 */
public class AyuMomentsActivity extends BaseNekoXSettingsActivity {

    private final CellGroup a = cellGroup = new CellGroup(this);

    private final AbstractConfigCell headerMain = cellGroup.appendCell(
            new ConfigCellHeader(LocaleController.getString(R.string.AyuMoments)));
    private final AbstractConfigCell ghostModeRow = cellGroup.appendCell(
            new ConfigCellText("GhostMode", () -> presentFragment(new NekoGhostModeActivity())));
    private final AbstractConfigCell regexFiltersRow = cellGroup.appendCell(
            new ConfigCellText("RegexFilters", () -> presentFragment(new RegexFiltersSettingActivity())));
    private final AbstractConfigCell drawerOrderRow = cellGroup.appendCell(
            new ConfigCellText("DrawerOrderTitle", () -> presentFragment(new DrawerMenuActivity())));

    private final AbstractConfigCell dividerPrivacy = cellGroup.appendCell(new ConfigCellDivider());
    private final AbstractConfigCell headerPrivacy = cellGroup.appendCell(
            new ConfigCellHeader(LocaleController.getString(R.string.AyuMomentsPrivacy)));
    private final AbstractConfigCell showGhostModeStatusRow = cellGroup.appendCell(
            new ConfigCellTextCheck(NekoConfig.showGhostModeStatus));
    private final AbstractConfigCell hideSponsoredMessageRow = cellGroup.appendCell(
            new ConfigCellTextCheck(NekoConfig.hideSponsoredMessage));
    private final AbstractConfigCell disableAdsRow = cellGroup.appendCell(
            new ConfigCellTextCheck(NekoConfig.disableAds));

    private final AbstractConfigCell dividerBlocking = cellGroup.appendCell(new ConfigCellDivider());
    private final AbstractConfigCell headerBlocking = cellGroup.appendCell(
            new ConfigCellHeader(LocaleController.getString(R.string.AyuMomentsBlocking)));
    private final AbstractConfigCell regexFiltersEnabledRow = cellGroup.appendCell(
            new ConfigCellTextCheck(NaConfig.INSTANCE.getRegexFiltersEnabled()));

    public AyuMomentsActivity() {
        setCanNotChange();
    }

    @Override
    public View createView(Context context) {
        View view = super.createView(context);
        listAdapter = new ListAdapter(context);
        listView.setAdapter(listAdapter);
        cellGroup.setListAdapter(listView, listAdapter);
        // Rows act on themselves; without this listener nothing responds to taps.
        listView.setOnItemClickListener(this::handleCellClick);
        return view;
    }

    @Override
    protected void setCanNotChange() {
        super.setCanNotChange();
        addRowsToMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getBaseGuid() {
        return 16000;
    }

    @Override
    public int getDrawable() {
        return R.drawable.msg2_reactions2;
    }

    @Override
    public String getTitle() {
        return LocaleController.getString(R.string.AyuMoments);
    }

    private class ListAdapter extends BaseListAdapter {

        ListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial, boolean divider) {
            AbstractConfigCell cell = cellGroup.rows.get(position);
            if (cell != null) {
                cell.onBindViewHolder(holder);
            }
        }

        @Override
        public int getItemViewType(int position) {
            AbstractConfigCell cell = cellGroup.rows.get(position);
            return cell == null ? 0 : cell.getType();
        }
    }
}
