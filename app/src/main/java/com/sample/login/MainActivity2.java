package com.sample.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private FirebaseDatabase database;

    //리싸이클러뷰 사용
    private RecyclerView recyclerView;
    private Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Path> arrayList;

    Button btn_go_main;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);



        recyclerView = (RecyclerView)findViewById(R.id.my_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); //객체를 담을 리스트

        //파이어베이스연동
        mFirebaseAuth = mFirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Login");
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();//내계정 연동

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false)) ; // 상하 스크롤


        mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child("path2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //파이어베이스 데이터베이스의 데이터를 받아오는 곳
                arrayList.clear(); //기존 배열 초기화
                for (DataSnapshot ds : snapshot.getChildren()){ //데이터 리스트 추출
                    Path path = ds.getValue(Path.class); //만들어준 PATH객체에 데이터를 담는다
                    arrayList.add(path); //담을 데이터들을 배열에 넣고 리싸이클러뷰로 보낼 준비
                }
                adapter.notifyDataSetChanged();//새로고침

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //db가져오던중 에러 발생시
                Log.e("MainActivity2",String.valueOf(error.toException()));

            }
        });

        adapter = new Adapter(arrayList,this);
        recyclerView.setAdapter(adapter); //리싸이클러뷰에 어댑터 연결

        btn_go_main = findViewById(R.id.btn_go_main);
        btn_go_main.setOnClickListener(new View.OnClickListener() {
            //뒤로가기 버튼
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        });




    }






    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView path_name;
        Button remove_path;
        //Button btn_share;


        ViewHolder(View itemView) {
            super(itemView);

            mFirebaseAuth = mFirebaseAuth.getInstance();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference("Login");
            path_name = itemView.findViewById(R.id.path_name);
            remove_path = itemView.findViewById(R.id.remove_path);
            //btn_share = itemView.findViewById(R.id.btn_share);

            remove_path.setOnClickListener(new View.OnClickListener() { //저장된 경로 지우기
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                    builder.setTitle("경로를 지우겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();//내계정 연동
                            String strText = path_name.getText().toString(); //경로명=strText

                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child("path2").child(strText).setValue(null); //경로이름만 있는 파일 삭제
                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child("path").child(strText).setValue(null); //실제 경로 삭제
                        }
                    });

                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    AlertDialog AL = builder.create();
                    AL.show();
                }
            });

/*
            btn_share.setOnClickListener(new View.OnClickListener() { //친구에게 경로 보내기
                @Override
                public void onClick(View view) {
                    String strText = path_name.getText().toString(); //경로명=strText
                    final EditText edittext = new EditText(MainActivity2.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                    builder.setTitle("경로 친구와 공유하기");
                    builder.setMessage("친구의 이메일을 입력하세요");
                    builder.setView(edittext);

                    builder.setPositiveButton("입력", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String friend_email = String.valueOf(edittext.getText());
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser(); //내계정 연동

                            //FirebaseUser firebaseUser2 = mFirebaseAuth.

                            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 Object my_path=snapshot.child("UserAccount").child(firebaseUser.getUid()).child("path").child(strText).getValue();
                                 Object my_path2=snapshot.child("UserAccount").child(firebaseUser.getUid()).child("path2").child(strText).getValue();

                                //mDatabaseRef.child("UserAccount").child()




                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });



                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //취소시
                        }
                    });

                }
            });

 */




        }

    }



    class Adapter extends RecyclerView.Adapter<com.sample.login.MainActivity2.ViewHolder> {
        private ArrayList<com.sample.login.Path> arrayList;
        private Context context;

        public Adapter(ArrayList<com.sample.login.Path> arrayList, Context context) {
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public com.sample.login.MainActivity2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_list, parent, false);
            com.sample.login.MainActivity2.ViewHolder viewholder = new ViewHolder(view);
            return viewholder;
        }


        @Override
        public void onBindViewHolder(@NonNull com.sample.login.MainActivity2.ViewHolder holder, int position) {

            FirebaseAuth mFirebaseAuth=FirebaseAuth.getInstance();
            DatabaseReference mDatabaseRef=FirebaseDatabase.getInstance().getReference("Login");
            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
            holder.path_name.setText(arrayList.get(position).getPath_name());
            String path_name=arrayList.get(position).getPath_name();
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //뷰 클릭시 실행
                    Intent intent=new Intent(context.getApplicationContext(),MainActivity.class);
                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            ArrayList<String> latitude_arr=new ArrayList<>();
                            ArrayList<String> longitude_arr=new ArrayList<>();

                            for(int i=0;i<100;i++){
                                //위도 경도 가져오기
                                Object latitude=snapshot.child("UserAccount").child(firebaseUser.getUid()).child("path").child(path_name).child(Integer.toString(i)).child("mapPointGeoCoord").child("latitude").getValue();
                                Object longitude=snapshot.child("UserAccount").child(firebaseUser.getUid()).child("path").child(path_name).child(Integer.toString(i)).child("mapPointGeoCoord").child("longitude").getValue();

                                if(latitude ==null){//해당 좌표가 없으면 break
                                    break;
                                }
                                //좌표 추가
                                latitude_arr.add(latitude.toString());
                                longitude_arr.add(longitude.toString());

                            }
                            //인텐트로 좌표값 넘기기
                            intent.putStringArrayListExtra("latitude_arr",latitude_arr);
                            intent.putStringArrayListExtra("longitude_arr",longitude_arr);

                            context.startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
/*
        //뷰 길게 눌러서 상호작용
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                remove(holder.getAdapterPosition());
                return true;
            }
        });

 */

        }
        @Override
        public int getItemCount() {

            return (arrayList !=null ? arrayList.size() : 0);
        }




        // 데이터를 입력
        public void setArrayData(String strData) {

        }
    }

}
class Path {
    private String path_name;

    public Path(){

    }

    public String getPath_name() {
        return path_name;
    }

    public void setPath_name(String path_name) {
        this.path_name = path_name;
    }
}

class Friend_email{
    private String emailId;
    public Friend_email(){

    }
    public String getEmail(){
        return emailId;
    }
    public void setEmail(String emailId){
        this.emailId=emailId;
    }


}
