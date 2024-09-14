package com.jw.media.jvideoplayer.player.youtube.dynamicparse;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.youtube.YoutubeHelper;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.MethodValue;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.SignSyntax;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.UrlSyntax;

import java.util.ArrayList;
import java.util.List;

public class GvAgainParser extends AbstractCipherParser implements IParserHandler {
    private static Logger logger = LoggerFactory.getLogger(GvAgainParser.class.getSimpleName());

    private UrlSyntax mUrlSyntax;

    @Override
    String parseUrl(String url) {
//        return YoutubeHelper.parseCipherUrl(url, true);
        logger.d("using gvAgain parser");
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

            List<MethodValue> gvAgainInvokes = new ArrayList<>();
            gvAgainInvokes.add(new MethodValue("KQ", 49));
            gvAgainInvokes.add(new MethodValue("KQ", 20));
            gvAgainInvokes.add(new MethodValue("KQ", 25));
            gvAgainInvokes.add(new MethodValue("d3", 1));
            mUrlSyntax.setMethodInvokes(gvAgainInvokes);
        }
        return mUrlSyntax;
    }
}
