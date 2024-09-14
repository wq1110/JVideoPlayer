package com.jw.media.jvideoplayer.player.youtube.dynamicparse;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.youtube.YoutubeHelper;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.MethodValue;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.SignSyntax;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.UrlSyntax;

import java.util.ArrayList;
import java.util.List;

public class GvParser extends AbstractCipherParser implements IParserHandler {
    private static Logger logger = LoggerFactory.getLogger(GvParser.class.getSimpleName());

    private UrlSyntax mUrlSyntax;

    @Override
    String parseUrl(String url) {
//        return YoutubeHelper.parseCipherUrl(url, false);
        logger.d("using gv parser");
        return YoutubeHelper.parseConfigCipherUrl(url, this);
    }

    @Override
    public String parseRegex() {
        return DefaultParserProvider.provideDefaultRegex();
    }

    @Override
    public SignSyntax parseSign() {
        return DefaultParserProvider.provideDefaultSignSyntax();
    }

    @Override
    public UrlSyntax parseUrl() {
        if (mUrlSyntax == null) {
            mUrlSyntax = new UrlSyntax();

            mUrlSyntax.setPrefix(DefaultParserProvider.providerDefaultPrefix());

            List<MethodValue> gvInvokes = new ArrayList<>();
            gvInvokes.add(new MethodValue("gT", 71));
            gvInvokes.add(new MethodValue("KQ", 19));
            gvInvokes.add(new MethodValue("KQ", 22));
            gvInvokes.add(new MethodValue("KQ", 66));
            gvInvokes.add(new MethodValue("gT", 79));
            gvInvokes.add(new MethodValue("KQ", 36));
            mUrlSyntax.setMethodInvokes(gvInvokes);
        }
        return mUrlSyntax;
    }
}
