package com.tripleyuan.winter.jdbc.with.tx;

import com.tripleyuan.winter.annotation.ComponentScan;
import com.tripleyuan.winter.annotation.Configuration;
import com.tripleyuan.winter.annotation.Import;
import com.tripleyuan.winter.jdbc.JdbcConfiguration;

@ComponentScan
@Configuration
@Import(JdbcConfiguration.class)
public class JdbcWithTxApplication {

}
