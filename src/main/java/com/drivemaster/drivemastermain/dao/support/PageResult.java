package com.drivemaster.drivemastermain.dao.support;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements) {
}
