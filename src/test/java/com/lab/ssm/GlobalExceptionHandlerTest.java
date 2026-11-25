package com.lab.ssm;

import com.lab.common.BusinessException;
import com.lab.common.GlobalExceptionHandler;
import com.lab.common.Result;
import com.lab.dto.WorkSaveDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 直接调用全局异常处理器，验证三类异常都被转成统一 Result。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_shouldReturnFieldMessage() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new WorkSaveDTO(), "work");
        bindingResult.addError(new FieldError("work", "workTitle", "作品标题不能为空"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter(), bindingResult);

        Result<Void> result = handler.handleValidationException(exception);
        printResult("参数校验异常", result);
        assertEquals(400, result.getCode());
        assertEquals("作品标题不能为空", result.getMessage());
    }

    @Test
    void handleBusinessException_shouldReturnBusinessCode() {
        Result<Void> result = handler.handleBusinessException(
                new BusinessException(404, "作品不存在"));
        printResult("业务异常", result);
        assertEquals(404, result.getCode());
        assertEquals("作品不存在", result.getMessage());
    }

    @Test
    void handleException_shouldHideInternalMessage() {
        Result<Void> result = handler.handleException(
                new RuntimeException("jdbc://secret 连接失败"));
        printResult("系统异常", result);
        assertEquals(500, result.getCode());
        assertEquals("系统异常，请联系管理员", result.getMessage());
    }

    private void printResult(String title, Result<?> result) {
        System.out.println("=== " + title + " ===");
        System.out.println("code=" + result.getCode()
                + ", message=" + result.getMessage()
                + ", data=" + result.getData());
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        return new MethodParameter(
                DummyController.class.getDeclaredMethod("dummy", WorkSaveDTO.class), 0);
    }

    static class DummyController {
        public void dummy(WorkSaveDTO dto) {
        }
    }
}
