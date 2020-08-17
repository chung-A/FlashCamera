package com.chung.flashcamera;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    String databaseName="FlashCamera_Database";
    String tableName="nowState";

    private ImageButton btn_ImageOnOff;
    private CameraManager cameraManager;
    //private AdView adView;

    private String cameraID;
    private Cursor c;
    MediaPlayer boySound;
    MediaPlayer girlSound;

    private boolean flashOn;
    private String imageState_girl=null;

    //String UnitID="ca-app-pub-4854979689590425~1749370875";
    //String AdID="ca-app-pub-4854979689590425/6810125867";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boySound=MediaPlayer.create(this,R.raw.boyt_sound);
        girlSound=MediaPlayer.create(this,R.raw.girl_sound);

        cameraManager=(CameraManager)getSystemService(CAMERA_SERVICE);
        btn_ImageOnOff=(ImageButton)findViewById(R.id.btn_flash_on);
        //adView=(AdView)findViewById(R.id.adView);
        //adView.setAdUnitId(UnitID);
        //adView.setAdSize(AdSize.BANNER);
        //AdRequest adRequest=new AdRequest.Builder().build();
        //adView.loadAd(adRequest);

        CreateDatabase();
        c=database.rawQuery("Select * from "+tableName+";",null);
        DataCall();

        flashOn=false;
        ImageSetting();

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
        {
            Toast.makeText(getApplicationContext(),"카메라 플래시기능이 지원되지 않습니다. 앱이 종료됩니다",Toast.LENGTH_LONG).show();

            DelayFinish();
            return;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataSave();
    }

    public void ClickedOn(View view)
    {
        btn_ImageOnOff.setSoundEffectsEnabled(false);
        if(imageState_girl=="girl")
        {
            girlSound.start();
        }
        else
        {
            boySound.start();
        }
        if(cameraID==null)
        {
            try
            {
                for(String id:cameraManager.getCameraIdList())
                {
                    CameraCharacteristics c= cameraManager.getCameraCharacteristics(id);
                    Boolean flashAvailable=c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    Integer lensFacing=c.get(CameraCharacteristics.LENS_FACING);
                    if(flashAvailable!=null&&flashAvailable&&lensFacing!=null&&lensFacing==CameraCharacteristics.LENS_FACING_BACK)
                    {
                        cameraID=id;
                        break;
                    }
                }
            }
            catch (CameraAccessException e)
            {
                cameraID=null;
                e.printStackTrace();
                return;
            }

        }

        flashOn=!flashOn;
        ImageSetting();

        try{
            cameraManager.setTorchMode(cameraID,flashOn);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    public void ClickedImageChange(View view)
    {
        if(imageState_girl=="girl"||imageState_girl==null)
        {
            imageState_girl="boy";
        }
        else
        {
            imageState_girl="girl";
        }

        DataSave();
        ImageSetting();
        return;
    }

    private void ImageSetting()
    {
        if(imageState_girl=="girl"||imageState_girl==null)
        {
            if(flashOn==false)
            {
                btn_ImageOnOff.setImageResource(R.drawable.off_girl);
            }
            else
            {
                btn_ImageOnOff.setImageResource(R.drawable.on_girl);
            }
        }
        else
        {
            if(flashOn==false)
            {
                btn_ImageOnOff.setImageResource(R.drawable.boy_off);
            }
            else
            {
                btn_ImageOnOff.setImageResource(R.drawable.boy_on);
            }
        }

        return;
    }

    private void DelayFinish(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },3500);
    }

    private void CreateDatabase()
    {
        database=openOrCreateDatabase(databaseName,MODE_PRIVATE,null);
        CreateTable();
    }

    private void CreateTable()
    {
        database.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+"("+"_id integer PRIMARY KEY autoincrement, "
                +"nowState text);");
        //1이면 여자.0이면 남자.
    }

    public void InsertData(int nowState)
    {
        database.execSQL("INSERT INTO "+tableName+" (videoURL) values ( "
                +"'"+nowState+"'"+");");
    }

    public void UpdateData() {
        ContentValues values=new ContentValues();
        values.put("nowState",imageState_girl);

        database.update(tableName,values,"nowState",null);
    }

    public void DataCall()
    {

        if(c.getCount()!=0) {
            imageState_girl = c.getString(1);
        }

        if(imageState_girl==null)
        {
            imageState_girl="girl";
        }

        return;
    }

    public void DataSave()
    {
        if(imageState_girl==null)
        {
            imageState_girl="girl";
        }

        UpdateData();
    }

}
