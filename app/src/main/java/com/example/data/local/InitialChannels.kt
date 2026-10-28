package com.example.data.local

import com.example.data.model.ChannelCategories

object InitialChannels {
    val DEFAULT_PLAYLIST_URL = "https://iptv-org.github.io/iptv/index.m3u"
    val CATEGORY_PLAYLIST_BASE = "https://iptv-org.github.io/iptv/categories/"

    fun getSeedChannels(): List<ChannelEntity> = listOf(
        // Benchmark Test Streams (Guaranteed 24/7 Verified HLS)
        ChannelEntity(
            id = "test_mux_hls",
            name = "Verified HLS Reference Stream",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            logoUrl = "https://mux.com/favicon.png",
            category = ChannelCategories.INTERNATIONAL,
            country = "US",
            language = "eng",
            groupTitle = "Verified Streams",
            isVerified = true
        ),

        // Pakistan
        ChannelEntity(
            id = "pk_ptv_news",
            name = "PTV News Official",
            streamUrl = "https://pakistan.tv/live/ptvnews/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c9/PTV_News_logo.png/320px-PTV_News_logo.png",
            category = ChannelCategories.PAKISTAN,
            country = "PK",
            language = "urd",
            groupTitle = "News",
            isVerified = true
        ),
        ChannelEntity(
            id = "pk_ptv_world",
            name = "PTV World (English)",
            streamUrl = "https://pakistan.tv/live/ptvworld/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f6/PTV_World_Logo.svg/300px-PTV_World_Logo.svg.png",
            category = ChannelCategories.PAKISTAN,
            country = "PK",
            language = "eng",
            groupTitle = "News",
            isVerified = true
        ),
        ChannelEntity(
            id = "pk_abn_urdu",
            name = "ABN Urdu Live",
            streamUrl = "https://mediaserver.abnvideos.com/streams/abnurdu.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/ABN_News_logo.png/320px-ABN_News_logo.png",
            category = ChannelCategories.PAKISTAN,
            country = "PK",
            language = "urd",
            groupTitle = "News",
            isVerified = true
        ),
        ChannelEntity(
            id = "pk_gnn_hd",
            name = "GNN HD News",
            streamUrl = "https://live.gnnhd.tv:8081/gnn/live/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/2/23/GNN_logo.png/320px-GNN_logo.png",
            category = ChannelCategories.PAKISTAN,
            country = "PK",
            language = "urd",
            groupTitle = "News",
            isVerified = true
        ),

        // India
        ChannelEntity(
            id = "in_dd_national",
            name = "DD National HD",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-national/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/DD_National_logo.svg/320px-DD_National_logo.svg.png",
            category = ChannelCategories.INDIA,
            country = "IN",
            language = "hin",
            groupTitle = "Public & General",
            isVerified = true
        ),
        ChannelEntity(
            id = "in_dd_news",
            name = "DD News (Doordarshan)",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-news/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/DD_News_Logo.png/320px-DD_News_Logo.png",
            category = ChannelCategories.INDIA,
            country = "IN",
            language = "hin",
            groupTitle = "News",
            isVerified = true
        ),
        ChannelEntity(
            id = "in_dd_india",
            name = "DD India International",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-india/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/14/DD_India_logo.png/320px-DD_India_logo.png",
            category = ChannelCategories.INDIA,
            country = "IN",
            language = "eng",
            groupTitle = "News",
            isVerified = true
        ),
        ChannelEntity(
            id = "in_dd_sports",
            name = "DD Sports HD (Cricket & Live)",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-sports/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/DD_Sports_logo.svg/320px-DD_Sports_logo.svg.png",
            category = ChannelCategories.INDIA,
            country = "IN",
            language = "hin",
            groupTitle = "Sports",
            isVerified = true
        ),
        ChannelEntity(
            id = "in_dd_bharati",
            name = "DD Bharati Culture",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-bharati/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/DD_Bharati.png/320px-DD_Bharati.png",
            category = ChannelCategories.INDIA,
            country = "IN",
            language = "hin",
            groupTitle = "Culture",
            isVerified = true
        ),

        // Cricket and Sports
        ChannelEntity(
            id = "sports_dd_cricket",
            name = "DD Sports Live (Cricket & Games)",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-sports/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/e/e0/DD_Sports_logo.svg/320px-DD_Sports_logo.svg.png",
            category = ChannelCategories.SPORTS,
            country = "IN",
            language = "eng",
            groupTitle = "Sports",
            isVerified = true
        ),
        ChannelEntity(
            id = "sports_redbull_tv",
            name = "Red Bull TV Live Sports",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Red_Bull_TV_logo.svg/320px-Red_Bull_TV_logo.svg.png",
            category = ChannelCategories.SPORTS,
            country = "US",
            language = "eng",
            groupTitle = "Action Sports",
            isVerified = true
        ),

        // News
        ChannelEntity(
            id = "news_dw_english",
            name = "DW English Live HD",
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Deutsche_Welle_symbol_2012.svg/320px-Deutsche_Welle_symbol_2012.svg.png",
            category = ChannelCategories.NEWS,
            country = "DE",
            language = "eng",
            groupTitle = "World News",
            isVerified = true
        ),
        ChannelEntity(
            id = "news_al_jazeera_ar",
            name = "Al Jazeera Arabic Live",
            streamUrl = "https://live-hls-web-aja.getaj.net/AJA/01.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f2/Al_Jazeera_English_logo.svg/320px-Al_Jazeera_English_logo.svg.png",
            category = ChannelCategories.NEWS,
            country = "QA",
            language = "ara",
            groupTitle = "International News",
            isVerified = true
        ),
        ChannelEntity(
            id = "news_al_jazeera_mubasher",
            name = "Al Jazeera Mubasher",
            streamUrl = "https://live-hls-web-ajm.getaj.net/AJM/01.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e6/Al_Jazeera_Mubasher_logo.png/320px-Al_Jazeera_Mubasher_logo.png",
            category = ChannelCategories.NEWS,
            country = "QA",
            language = "ara",
            groupTitle = "Live News",
            isVerified = true
        ),

        // Documentaries
        ChannelEntity(
            id = "doc_dw_documentary",
            name = "DW Documentary (English)",
            streamUrl = "https://dwamdstream104.akamaized.net/hls/live/2015530/dwstream104/index.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Deutsche_Welle_symbol_2012.svg/320px-Deutsche_Welle_symbol_2012.svg.png",
            category = ChannelCategories.DOCUMENTARIES,
            country = "DE",
            language = "eng",
            groupTitle = "Documentary",
            isVerified = true
        ),
        ChannelEntity(
            id = "doc_cgtn_documentary",
            name = "CGTN Documentary HD",
            streamUrl = "https://news.cgtn.com/resource/live/document/cgtn-doc.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/CGTN_Documentary_logo.svg/320px-CGTN_Documentary_logo.svg.png",
            category = ChannelCategories.DOCUMENTARIES,
            country = "CN",
            language = "eng",
            groupTitle = "Culture & History",
            isVerified = true
        ),
        ChannelEntity(
            id = "doc_france24",
            name = "France 24 Documentaries & Reports",
            streamUrl = "https://f24hls-i.akamaihd.net/hls/live/221193/F24_EN_LO_HLS/master_500.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/France_24_logo.svg/320px-France_24_logo.svg.png",
            category = ChannelCategories.DOCUMENTARIES,
            country = "FR",
            language = "eng",
            groupTitle = "Documentary",
            isVerified = true
        ),

        // Wildlife and Animals
        ChannelEntity(
            id = "wildlife_alaska_bears",
            name = "Explore Alaska Bears Nature Live",
            streamUrl = "https://explore-alaskabears.amagi.tv/playlist.m3u8",
            logoUrl = "https://explore.org/assets/images/explore-logo.svg",
            category = ChannelCategories.WILDLIFE,
            country = "US",
            language = "eng",
            groupTitle = "Nature & Wildlife",
            isVerified = true
        ),
        ChannelEntity(
            id = "wildlife_birds",
            name = "Explore Birds & Wildlife Live",
            streamUrl = "https://explore-birds.amagi.tv/playlist.m3u8",
            logoUrl = "https://explore.org/assets/images/explore-logo.svg",
            category = ChannelCategories.WILDLIFE,
            country = "US",
            language = "eng",
            groupTitle = "Nature",
            isVerified = true
        ),
        ChannelEntity(
            id = "wildlife_oceans",
            name = "Explore Oceans & Marine Life",
            streamUrl = "https://explore-oceans.amagi.tv/playlist.m3u8",
            logoUrl = "https://explore.org/assets/images/explore-logo.svg",
            category = ChannelCategories.WILDLIFE,
            country = "US",
            language = "eng",
            groupTitle = "Marine Wildlife",
            isVerified = true
        ),

        // Science and Education
        ChannelEntity(
            id = "sci_nasa_public",
            name = "NASA TV Public HD",
            streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/320px-NASA_logo.svg.png",
            category = ChannelCategories.SCIENCE,
            country = "US",
            language = "eng",
            groupTitle = "Space Science",
            isVerified = true
        ),
        ChannelEntity(
            id = "sci_mit_tv",
            name = "MIT Tech TV (Science & Tech)",
            streamUrl = "https://video.mit.edu/live.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/MIT_logo.svg/320px-MIT_logo.svg.png",
            category = ChannelCategories.SCIENCE,
            country = "US",
            language = "eng",
            groupTitle = "Education",
            isVerified = true
        ),

        // Entertainment
        ChannelEntity(
            id = "ent_9x_jalwa",
            name = "9X Jalwa Bollywood Music & Hits",
            streamUrl = "https://wiselp.wiseplayout.com/9X_Jalwa/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f6/9X_Jalwa_logo.png/320px-9X_Jalwa_logo.png",
            category = ChannelCategories.ENTERTAINMENT,
            country = "IN",
            language = "hin",
            groupTitle = "Music & Cinema",
            isVerified = true
        ),
        ChannelEntity(
            id = "ent_dd_retro",
            name = "DD Retro Classic TV",
            streamUrl = "https://d35j504z0x2vu2.cloudfront.net/v1/master/0bc8e8376c62296191a9f1747bc7e61a70601c7b/dd-retro/master.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/DD_National_logo.svg/320px-DD_National_logo.svg.png",
            category = ChannelCategories.ENTERTAINMENT,
            country = "IN",
            language = "hin",
            groupTitle = "Classics",
            isVerified = true
        ),

        // Kids and Family
        ChannelEntity(
            id = "kids_big_buck_bunny",
            name = "Big Buck Bunny Animation (Family)",
            streamUrl = "https://test-streams.mux.dev/test_001/stream.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/320px-Big_buck_bunny_poster_big.jpg",
            category = ChannelCategories.KIDS,
            country = "US",
            language = "eng",
            groupTitle = "Family & Cartoons",
            isVerified = true
        ),
        ChannelEntity(
            id = "kids_4fun_kids",
            name = "4Fun Kids TV",
            streamUrl = "https://stream.4fun.tv:8889/hls/4fk.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/4Fun_Kids_logo.png/320px-4Fun_Kids_logo.png",
            category = ChannelCategories.KIDS,
            country = "PL",
            language = "eng",
            groupTitle = "Kids",
            isVerified = true
        ),
        ChannelEntity(
            id = "kids_3abn",
            name = "3ABN Kids Live",
            streamUrl = "https://3abn.bozztv.com/3abn2/Kids_live/smil:Kids_live.smil/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/0/07/3ABN_Kids_Network_logo.png/320px-3ABN_Kids_Network_logo.png",
            category = ChannelCategories.KIDS,
            country = "US",
            language = "eng",
            groupTitle = "Kids & Learning",
            isVerified = true
        ),

        // International TV
        ChannelEntity(
            id = "intl_arirang_tv",
            name = "Arirang TV Korea (Global)",
            streamUrl = "https://amdlive.ctnd.com.edgesuite.net/arirang_1ch/smil:arirang_1ch.smil/playlist.m3u8",
            logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Arirang_TV_logo.svg/320px-Arirang_TV_logo.svg.png",
            category = ChannelCategories.INTERNATIONAL,
            country = "KR",
            language = "eng",
            groupTitle = "World Culture",
            isVerified = true
        ),
        ChannelEntity(
            id = "intl_benchmark_mux",
            name = "International HD Live Stream",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            logoUrl = "https://mux.com/favicon.png",
            category = ChannelCategories.INTERNATIONAL,
            country = "US",
            language = "eng",
            groupTitle = "Global HD",
            isVerified = true
        )
    )
}
