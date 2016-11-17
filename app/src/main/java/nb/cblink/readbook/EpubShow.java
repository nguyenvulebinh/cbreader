package nb.cblink.readbook;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import nb.cblink.epublib.BookSection;
import nb.cblink.epublib.CssStatus;
import nb.cblink.epublib.Reader;
import nb.cblink.epublib.exception.OutOfPagesException;
import nb.cblink.epublib.exception.ReadingException;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.TOP;

public class EpubShow extends AppCompatActivity implements PageFragment.OnFragmentReadyListener {

    private Reader reader;

    private ViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private int pageCount = Integer.MAX_VALUE;
    private int pxScreenWidth;

    private boolean isPickedWebView = true;
    private boolean isSkippedToPage = false;

    ProgressDialog progress = null;
    private Toast pageNumberToast;
    private String asynContent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.epub_activity);
        if (getIntent() != null && getIntent().getExtras() != null) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setOffscreenPageLimit(0);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            pxScreenWidth = getResources().getDisplayMetrics().widthPixels;
            String filePath = getIntent().getExtras().getString("filePath");
            try {
                reader = new Reader();
                reader.setFullContent(filePath);
                reader.setMaxContentPerSection(900);
                reader.setCssStatus(isPickedWebView ? CssStatus.INCLUDE : CssStatus.OMIT);
                reader.setIsIncludingTextContent(true);
                reader.setIsOmittingTitleTag(true);
            } catch (ReadingException e) {
                Toast.makeText(EpubShow.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public View onFragmentReady(int position) {
        BookSection bookSection = null;

        if (asynContent == null) {

            try {
                bookSection = reader.readSection(position);
            } catch (ReadingException e) {
                e.printStackTrace();
                Toast.makeText(EpubShow.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (OutOfPagesException e) {
                e.printStackTrace();
                this.pageCount = e.getPageCount();

                if (isSkippedToPage) {
                    Toast.makeText(EpubShow.this, "Max page number is: " + this.pageCount, Toast.LENGTH_LONG).show();
                }

                mSectionsPagerAdapter.notifyDataSetChanged();
            } catch (Exception e) {

            }
            isSkippedToPage = false;

            if (bookSection != null) {
                pageNumberToast = Toast.makeText(this, position + "", Toast.LENGTH_SHORT);
                pageNumberToast.setGravity(LEFT | TOP, 0, getSupportActionBar().getHeight());
                pageNumberToast.show();
                return setFragmentView(isPickedWebView, bookSection.getSectionContent(), "text/html", "UTF-8"); // reader.isContentStyled
            }
        } else {
            pageNumberToast = Toast.makeText(this, position + "", Toast.LENGTH_SHORT);
            pageNumberToast.setGravity(LEFT | TOP, 0, getSupportActionBar().getHeight());
            pageNumberToast.show();
            String temp = asynContent;
            asynContent = null;
            return setFragmentView(isPickedWebView, temp, "text/html", "UTF-8"); // reader.isContentStyled
        }
        return null;

    }

    private View setFragmentView(boolean isContentStyled, String data, String mimeType, String encoding) {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (isContentStyled) {
            WebView webView = new WebView(EpubShow.this);
            webView.loadDataWithBaseURL(null, data, mimeType, encoding, null);
            webView.setLayoutParams(layoutParams);

            return webView;
        } else {
            ScrollView scrollView = new ScrollView(EpubShow.this);
            scrollView.setLayoutParams(layoutParams);

            TextView textView = new TextView(EpubShow.this);
            textView.setLayoutParams(layoutParams);

            textView.setText(Html.fromHtml(data, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    String imageAsStr = source.substring(source.indexOf(";base64,") + 8);
                    byte[] imageAsBytes = Base64.decode(imageAsStr, Base64.DEFAULT);
                    Bitmap imageAsBitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                    int imageWidthStartPx = (pxScreenWidth - imageAsBitmap.getWidth()) / 2;
                    int imageWidthEndPx = pxScreenWidth - imageWidthStartPx;

                    Drawable imageAsDrawable = new BitmapDrawable(getResources(), imageAsBitmap);
                    imageAsDrawable.setBounds(imageWidthStartPx, 0, imageWidthEndPx, imageAsBitmap.getHeight());
                    return imageAsDrawable;
                }
            }, null));

            int pxPadding = dpToPx(12);

            textView.setPadding(pxPadding, pxPadding, pxPadding, pxPadding);

            scrollView.addView(textView);
            return scrollView;
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return PageFragment.newInstance(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.next, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gotopageView:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.gotopage_pages);
                View dialogButton = dialog.findViewById(R.id.goToButton);
                final EditText editText = (EditText) dialog.findViewById(R.id.pageNumberTextEdit);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncTask<Void, Void, Void>() {
                            BookSection bookSection;
                            int index = Integer.parseInt(editText.getText().toString());
                            boolean outofrage = false;
                            int pagecounts = 0;

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                progress = ProgressDialog.show(EpubShow.this, "Decode",
                                        "Progress...", true);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    bookSection = reader.readSection(index);
                                } catch (ReadingException e) {
                                    e.printStackTrace();
                                } catch (OutOfPagesException e) {
                                    e.printStackTrace();
                                    pagecounts = e.getPageCount();

                                    outofrage = true;
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                progress.dismiss();
                                if (!outofrage) {
                                    asynContent = bookSection.getSectionContent();
                                    mViewPager.setCurrentItem(Integer.parseInt(editText.getText().toString()));
                                } else {
                                    Toast.makeText(EpubShow.this, "Max page number is: " + pagecounts, Toast.LENGTH_LONG).show();
                                }
                            }
                        }.execute();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
        }
        return false;
    }
}
