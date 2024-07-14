package com.nhutkhuong1.application.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateCommentProductRequest {
    private String productId;
    @NotBlank(message = "Nội dung bình luận trống!")
    private String content;
}
