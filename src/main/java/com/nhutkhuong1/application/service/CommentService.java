package com.nhutkhuong1.application.service;

import com.nhutkhuong1.application.entity.Comment;
import com.nhutkhuong1.application.model.request.CreateCommentPostRequest;
import com.nhutkhuong1.application.model.request.CreateCommentProductRequest;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    Comment createCommentPost(CreateCommentPostRequest createCommentPostRequest, long userId);
    Comment createCommentProduct(CreateCommentProductRequest createCommentProductRequest, long userId);
}
