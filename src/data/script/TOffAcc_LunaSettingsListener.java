package data.script;

import data.ids.TOffAcc_RelevantConstants;
import lunalib.lunaSettings.LunaSettingsListener;

public class TOffAcc_LunaSettingsListener implements LunaSettingsListener
{
    @Override
    public void settingsChanged(String modID)
    {
        if (modID.equals(TOffAcc_RelevantConstants.TOFFACC_MODID))TOffAcc_RelevantConstants.reload();
    }
}
