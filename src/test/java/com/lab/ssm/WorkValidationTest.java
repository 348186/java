package com.lab.ssm;

import com.lab.dto.WorkSaveDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 不依赖数据库的 JSR-303 校验单元测试。
 */
class WorkValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void invalidWork_shouldBeRejected() {
        WorkSaveDTO dto = new WorkSaveDTO();
        dto.setWorkTitle("");
        dto.setPhotographerId(null);
        dto.setStyle("");
        dto.setPrice(new BigDecimal("-1"));
        dto.setStatus(9);

        Set<ConstraintViolation<WorkSaveDTO>> violations = validator.validate(dto);
        printViolations("非法作品参数校验", violations);
        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(messages.contains("作品标题不能为空"));
        assertTrue(messages.contains("摄影师编号不能为空"));
        assertTrue(messages.contains("作品定价不能为负数"));
        assertTrue(messages.contains("展示状态只能是0或1"));
    }

    @Test
    void validWork_shouldPass() {
        WorkSaveDTO dto = new WorkSaveDTO();
        dto.setPhotographerId(1);
        dto.setWorkTitle("晨光里的她");
        dto.setLocation("上海 武康路");
        dto.setShootDate(new java.util.Date());
        dto.setStyle("人像");
        dto.setPrice(new BigDecimal("1200.00"));
        dto.setStatus(1);

        Set<ConstraintViolation<WorkSaveDTO>> violations = validator.validate(dto);
        printViolations("合法作品参数校验", violations);
        assertEquals(0, violations.size());
    }

    private void printViolations(String title, Set<ConstraintViolation<WorkSaveDTO>> violations) {
        System.out.println("=== " + title + " ===");
        if (violations.isEmpty()) {
            System.out.println("无违规字段");
            return;
        }
        for (ConstraintViolation<WorkSaveDTO> violation : violations) {
            System.out.println(violation.getPropertyPath() + " -> " + violation.getMessage());
        }
    }
}
