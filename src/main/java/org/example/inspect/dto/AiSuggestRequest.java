package org.example.inspect.dto;

public class AiSuggestRequest {

    /** 用户补充上下文，可选 */
    private String userNote;

    public String getUserNote() { return userNote; }
    public void setUserNote(String userNote) { this.userNote = userNote; }
}
