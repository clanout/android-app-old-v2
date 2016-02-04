package reaper.android.app.ui.activity.dummy;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import reaper.android.R;
import reaper.android.app.model.EventCategory;
import reaper.android.app.ui.util.DrawableFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class DummyActivity extends AppCompatActivity
{
    ImageSwitcher imageSwitcher;
    Button btnChange;

    private static List<Drawable> introImages = Arrays.asList(
            DrawableFactory.get(EventCategory.DRINKS, 120),
            DrawableFactory.get(EventCategory.INDOORS,120),
            DrawableFactory.get(EventCategory.OUTDOORS, 120),
            DrawableFactory.get(EventCategory.CAFE, 120)
            );

    private int activePosition;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        btnChange = (Button) findViewById(R.id.btnChange);

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory()
        {
            @Override
            public View makeView()
            {
                return new ImageView(getApplicationContext());
            }
        });

        imageSwitcher.setInAnimation(this, android.R.anim.slide_in_left);
        imageSwitcher.setOutAnimation(this, android.R.anim.slide_out_right);

        activePosition = 0;
        imageSwitcher.setImageDrawable(introImages.get(activePosition));

//        btnChange.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                activePosition++;
//                if (activePosition >= introImages.size())
//                {
//                    activePosition = 0;
//                }
//                imageSwitcher.setImageDrawable(introImages.get(activePosition));
//            }
//        });

        Observable
                .interval(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Long aLong)
                    {
                        activePosition++;
                        if (activePosition >= introImages.size())
                        {
                            activePosition = 0;
                        }
                        imageSwitcher.setImageDrawable(introImages.get(activePosition));
                    }
                });
    }
}
