package reaper.android.app.ui.screens.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import reaper.android.R;
import reaper.android.app.model.ChatMessage;


/**
 * Created by harsh on 21-05-2015.
 */
public class ChatAdapter extends BaseAdapter
{

    private List<ChatMessage> chatMessageList;
    private Context context;

    public ChatAdapter(List<ChatMessage> chatMessageList, Context context)
    {
        this.chatMessageList = chatMessageList;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        if (chatMessageList == null)
        {
            return 0;
        }
        else
        {
            return chatMessageList.size();
        }
    }

    @Override
    public ChatMessage getItem(int position)
    {
        if (chatMessageList != null)
        {
            return chatMessageList.get(position);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ChatViewHolder chatViewHolder = new ChatViewHolder();
        ChatMessage chatMessageCurrent = chatMessageList.get(position);
        ChatMessage chatMessagePrevious = null;

        if (position != 0)
        {
            chatMessagePrevious = chatMessageList.get(position - 1);
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.list_item_chat, null);
            chatViewHolder = createViewHolder(convertView);
            convertView.setTag(chatViewHolder);
        }
        else
        {
            chatViewHolder = (ChatViewHolder) convertView.getTag();
        }
        boolean isMe = chatMessageCurrent.isMe();
        setAlignment(chatViewHolder, isMe);

        if (chatMessagePrevious == null)
        {
            chatViewHolder.infoOutside.setVisibility(View.VISIBLE);
            chatViewHolder.messageInside.setText(chatMessageCurrent.getMessage());
            chatViewHolder.infoOutside.setText(chatMessageCurrent.getSenderName());

        }
        else
        {
            if (chatMessageCurrent.getSenderId().equals(chatMessagePrevious.getSenderId()))
            {
                chatViewHolder.infoOutside.setVisibility(View.GONE);
                chatViewHolder.messageInside.setText(chatMessageCurrent.getMessage());
            }
            else
            {
                chatViewHolder.infoOutside.setVisibility(View.VISIBLE);
                chatViewHolder.messageInside.setText(chatMessageCurrent.getMessage());
                chatViewHolder.infoOutside.setText(chatMessageCurrent.getSenderName());

            }
        }
        return convertView;
    }

    private void setAlignment(ChatViewHolder chatViewHolder, boolean isMe)
    {
        if (!isMe)
        {
            chatViewHolder.contentWithBackground.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) chatViewHolder.contentWithBackground.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            chatViewHolder.contentWithBackground.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) chatViewHolder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            chatViewHolder.content.setLayoutParams(lp);

            layoutParams = (LinearLayout.LayoutParams) chatViewHolder.messageInside.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            chatViewHolder.messageInside.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) chatViewHolder.infoOutside.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            chatViewHolder.infoOutside.setLayoutParams(layoutParams);
        }
        else
        {

            chatViewHolder.contentWithBackground.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) chatViewHolder.contentWithBackground.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            chatViewHolder.contentWithBackground.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) chatViewHolder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            chatViewHolder.content.setLayoutParams(lp);

            layoutParams = (LinearLayout.LayoutParams) chatViewHolder.messageInside.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            chatViewHolder.messageInside.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) chatViewHolder.infoOutside.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            chatViewHolder.infoOutside.setLayoutParams(layoutParams);
        }
    }

    public void add(ChatMessage message)
    {
        chatMessageList.add(message);
    }

    public void add(List<ChatMessage> messages)
    {
        chatMessageList.addAll(messages);
    }

    private ChatViewHolder createViewHolder(View v)
    {
        ChatViewHolder holder = new ChatViewHolder();
        holder.messageInside = (TextView) v.findViewById(R.id.tvTextMessageInBubble);
        holder.infoOutside = (TextView) v.findViewById(R.id.tvTextContentName);
        holder.content = (LinearLayout) v.findViewById(R.id.llListItemChat);
        holder.contentWithBackground = (LinearLayout) v.findViewById(R.id.llChatContentWithBackground);
        return holder;
    }

    private static class ChatViewHolder
    {
        public TextView messageInside, infoOutside;
        public LinearLayout content, contentWithBackground;
    }
}
