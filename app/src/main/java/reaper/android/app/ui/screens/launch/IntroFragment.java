package reaper.android.app.ui.screens.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import reaper.android.R;
import reaper.android.app.ui._core.BaseFragment;

public class IntroFragment extends BaseFragment
{
    private static final String ARG_POSITION = "arg_position";

    /* UI Elements */
    @Bind(R.id.rlIntroContainer)
    View rlIntroContainer;

    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.ivIntroIcon)
    ImageView ivIntroIcon;

    public static IntroFragment newInstance(int position)
    {
        IntroFragment fragment = new IntroFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    /* Lifecycle Methods */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        int position = getArguments().getInt(ARG_POSITION, 0);
        switch (position)
        {
            case 0:
                rlIntroContainer.setBackgroundResource(R.color.intro_1);
                tvTitle.setText(R.string.intro_title_1);
                ivIntroIcon.setImageResource(R.drawable.intro_1);
                break;

            case 1:
                rlIntroContainer.setBackgroundResource(R.color.intro_2);
                tvTitle.setText(R.string.intro_title_2);
                ivIntroIcon.setImageResource(R.drawable.intro_2);
                break;

            case 2:
                rlIntroContainer.setBackgroundResource(R.color.intro_3);
                tvTitle.setText(R.string.intro_title_3);
                ivIntroIcon.setImageResource(R.drawable.intro_3);
                break;

            case 3:
                rlIntroContainer.setBackgroundResource(R.color.intro_4);
                tvTitle.setText(R.string.intro_title_4);
                ivIntroIcon.setImageResource(R.drawable.intro_4);
                break;
        }
    }
}
