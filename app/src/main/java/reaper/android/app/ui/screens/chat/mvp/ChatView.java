package reaper.android.app.ui.screens.chat.mvp;

import reaper.android.app.model.ChatMessage;

public interface ChatView
{
    void displayTitle(String title);

    void displayMessage(ChatMessage chatMessage);

    void displaySendMessageFailureError();

    void displayError();

    void onHistoryLoaded();

    void displayNoMoreHistory();
}
