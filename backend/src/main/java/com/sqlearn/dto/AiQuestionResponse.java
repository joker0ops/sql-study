package com.sqlearn.dto;

import java.util.List;

public record AiQuestionResponse(String question, String difficulty, String hint, List<String> tables) {
}
