package com.gongxifacai.gongxifacai.util;

import java.math.BigDecimal;

public class BigDecimalUtil {
    public static boolean isGreaterThanZero(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isEqualToZero(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isLessThanZero(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) < 0;
    }
}
