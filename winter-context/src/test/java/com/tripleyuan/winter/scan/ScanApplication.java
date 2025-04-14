package com.tripleyuan.winter.scan;

import com.tripleyuan.winter.imported.LocalDateConfiguration;
import com.tripleyuan.winter.imported.ZonedDateConfiguration;
import com.tripleyuan.winter.annotation.ComponentScan;
import com.tripleyuan.winter.annotation.Import;

@ComponentScan
@Import({ LocalDateConfiguration.class, ZonedDateConfiguration.class })
public class ScanApplication {

}
