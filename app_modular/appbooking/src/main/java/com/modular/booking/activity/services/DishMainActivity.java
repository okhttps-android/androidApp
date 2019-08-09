package com.modular.booking.activity.services;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.system.DisplayUtil;
import com.core.base.OABaseActivity;
import com.core.utils.CommonUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.DrawableCenterTextView;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.activity.utils.ShoppingCart;
import com.modular.booking.activity.utils.ShoppingCartPanel;
import com.modular.booking.adapter.ItemDishCategoryListAdapter;
import com.modular.booking.adapter.ItemDishPinHeaderAdapter;
import com.modular.booking.adapter.LayoutShoppingCartItemAdapter;
import com.modular.booking.model.Product;
import com.modular.booking.model.ProductCategory;
import com.modular.booking.model.ShoppingEntity;
import com.modular.booking.utils.EventMessage;
import com.modular.booking.utils.RxBus;
import com.modular.booking.widget.PinnedHeaderListView;
import com.modular.booking.widget.ShoppingCountView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
  * @desc:选择菜品
  * @author：Arison on 2018/1/24
  */
public class DishMainActivity extends OABaseActivity {
    
    public static final String TAG = "DishSelectActivity";
    private CircleImageView iv_header;
    private TextView tv_title;
    private TextView tv_sub;
    private TextView num_tv;
    private BottomSheetLayout  mBottmSheetLayout;
    private LinearLayout ll_bottom;
    private ListView lv_product_category;
    private PinnedHeaderListView lv_product;

    private ShoppingCartPanel mShoppingCartPanel;

    private Button bt_bottom;
    private ImageView max_img;
    private ImageView mBackImageView;
    private DrawableCenterTextView mSearchTv;
    private RelativeLayout mRelativeTop;
    private Drawable drawBg;

    private ImageView ivShopCar;
    private TextView tvShopSure;
    private TextView tvTotalPrice;
    
    private List<ProductCategory> productCategories=new ArrayList<>();
    List<ProductCategory> productAllCategorys=new ArrayList<>();
    private ItemDishCategoryListAdapter itemDishCategoryListAdapter;
    //private ItemDishPinnedListAdapter itemDishPinnedListAdapter;
    
    private ItemDishPinHeaderAdapter itemDishPinHeaderAdapter;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_main);
        initView();
        initEvent();
        initData();
    }
    
    private void initView(){
        mShoppingCartPanel = new ShoppingCartPanel(this);
        lv_product_category= findViewById(R.id.lv_product_category);
        mBottmSheetLayout=findViewById(R.id.bottom_sheet_layout);
        lv_product=findViewById(R.id.lv_dish_product);
        ll_bottom=findViewById(R.id.ll_bottom);
        bt_bottom=findViewById(R.id.bt_bottom);
        iv_header =findViewById(R.id.iv_header);
        max_img = findViewById(R.id.max_img);
        tv_title =findViewById(R.id.tv_title);
        tv_sub =findViewById(R.id.tv_sub);
        ivShopCar=findViewById(R.id.ivShopCar);
        tvShopSure=findViewById(R.id.tvShopSure);
        tvTotalPrice=findViewById(R.id.tvTotalPrice);
        num_tv=findViewById(R.id.num_tv);
        
        itemDishCategoryListAdapter=new ItemDishCategoryListAdapter(DishMainActivity.this,productCategories);
        lv_product_category.setAdapter( itemDishCategoryListAdapter);
        lv_product_category.setSelection(0);
        
        
//        itemDishPinnedListAdapter=new ItemDishPinnedListAdapter(mContext,productAllCategorys);
//        itemDishPinnedListAdapter.setAnimTargetView(ivShopCar);

        itemDishPinHeaderAdapter=new ItemDishPinHeaderAdapter(this);
        itemDishPinHeaderAdapter.setAnimTargetView(num_tv);
        lv_product.setAdapter(itemDishPinHeaderAdapter);

        Intent data=getIntent();
        if (data!=null) {
            String sb_imageurl=data.getStringExtra("headImgUrl");
            String tvsub=data.getStringExtra("tvSub");
            String tvtitle=data.getStringExtra("tvTitle");
            setTitle(tvtitle);
            tv_title.setText(tvtitle);
            tv_sub.setText(tvsub);
            AvatarHelper.getInstance().display(sb_imageurl, iv_header, true);
            AvatarHelper.getInstance().display(sb_imageurl, max_img, true);
        }

    }
    
    private boolean isClickTrigger=true;
    
    private void initEvent(){
        mBottmSheetLayout.addOnSheetStateChangeListener(new BottomSheetLayout.OnSheetStateChangeListener() {
            @Override
            public void onSheetStateChanged(BottomSheetLayout.State state) {
                LogUtil.d(TAG,"BottomSheetLayout.State:"+state);
            }
        });
        tvShopSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //先确保购物车不为空
                if (!ListUtils.isEmpty(ShoppingCart.getInstance().getShoppingList())) {
                    setResult(0x01,new Intent().putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) ShoppingCart.getInstance().getShoppingList()));
                    finish();
                }else{
                    ToastMessage("您的购物车是空的哦");
                }
            }
        });
        
        ll_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShoppingCartPanel();
                //showShopWindow(view);
            }
        });

        lv_product_category.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemDishCategoryListAdapter.setSelectIndex(position);
                itemDishCategoryListAdapter.notifyDataSetChanged();
                int productPos = 0;
                for (int index = 0; index < position; index++) {
                    productPos += itemDishPinHeaderAdapter.getCountForSection(index) + 1;
                }
                isClickTrigger = true;
                lv_product.setSelection(productPos);
            }
        });

   
        lv_product.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isClickTrigger) {
                    isClickTrigger = false;
                } else {
                    int section = itemDishPinHeaderAdapter.getSectionForPosition(firstVisibleItem);
                    itemDishCategoryListAdapter.setSelectIndex(section);
                    isClickTrigger = true;//非常关键的一点
                    itemDishCategoryListAdapter.notifyDataSetChanged();//会触发onScroll事件，导致循环执行
                    LogUtil.d(TAG,"LastVisiblePosition:"+lv_product_category.getLastVisiblePosition()+" section:"+section
                    +"FirstVisiblePosition:"+lv_product_category.getFirstVisiblePosition());
                    if (section>=lv_product_category.getLastVisiblePosition()){
                        lv_product_category.smoothScrollToPosition(lv_product_category.getLastVisiblePosition()+2);
                    }
                    if (section<=lv_product_category.getFirstVisiblePosition()){
                        lv_product_category.smoothScrollToPosition(lv_product_category.getFirstVisiblePosition()-2);
                    }
                    if (firstVisibleItem == 0) {
                        View firstVisibleItemView = lv_product.getChildAt(0);
                        if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
                            LogUtil.d(TAG,"scroll top"+0);
                            lv_product_category.smoothScrollToPosition(0);
                        }
                    }
                    if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                        View lastVisibleItemView = lv_product.getChildAt(lv_product.getChildCount() - 1);
                        if (lastVisibleItemView != null && lastVisibleItemView.getBottom() == lv_product.getHeight()) {
                            LogUtil.d(TAG,"scroll bottom"+itemDishCategoryListAdapter.getCount());
                            lv_product_category.smoothScrollToPosition(itemDishCategoryListAdapter.getCount()-1);
                        }
                    }
                   
                }

            }
        });
        
        lv_product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view.getTag() instanceof  ItemDishPinHeaderAdapter.ItemViewHolder){
                    ItemDishPinHeaderAdapter.ItemViewHolder itemViewHolder= ( ItemDishPinHeaderAdapter.ItemViewHolder) view.getTag();
                    final Product product=itemViewHolder.product;
                    LogUtil.d(TAG,"product name:"+product.getName());
                    itemViewHolder.img_product_photo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopupWindow(view,product);
                        }
                    });
                    itemViewHolder.txt_product_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopupWindow(view,product);
                        }
                    });
                    itemViewHolder.txt_product_month_sales.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopupWindow(view,product);
                        }
                    });
                    
                    itemViewHolder.txt_product_price.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showPopupWindow(view,product);
                        }
                    });
                }
                
            }
        });
        
        mSubscription= RxBus.getInstance().toObservable()
                .filter(o -> o instanceof EventMessage)
                .map(o -> (EventMessage) o)
                 .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(o -> onShoppingCartChange(o.getObject()))
                .subscribe();
    }
 
    private void initData(){
          HttpClient httpClient=new HttpClient.Builder("http://192.168.253.200:8080/Chapter/").build();
                 httpClient.Api().send(new HttpClient.Builder()
                 .url("data/dish")
                 .method(Method.GET)
                 .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {
          
                     @Override
                     public void onResponse(Object o) {
                       //  LogUtil.d(TAG,o.toString());
//                         try {
                             String data=   JSON.parseObject(o.toString()).getString("Data");
                             JSONArray dishCategories= JSON.parseObject(data).getJSONArray("DishCategories");
                             JSONArray dishs=JSON.parseObject(data).getJSONArray("Dishs");
                             List<ProductCategory> productCategorys= JSON.parseArray(dishCategories.toJSONString(), ProductCategory.class);
                             productCategories.addAll(productCategorys);
                             itemDishCategoryListAdapter.notifyDataSetChanged();
                             List<Product> products= JSON.parseArray(dishs.toJSONString(), Product.class);
                             for (int i = 0; i < productCategorys.size(); i++) {
                                // LogUtil.d(TAG,"菜类："+productCategorys.get(i).getName());
                                 String code=productCategorys.get(i).getCode();  //关联菜单
                                 String name=productCategorys.get(i).getName();
                                 productCategorys.get(i).setDishCategoryId(i);
                                 productAllCategorys.add(productCategorys.get(i));
                                 //productCategories.add(productCategorys.get(i));
                                 for (int j = 0; j < products.size(); j++) {
                                     String dishCategoryCode=products.get(j).getDishCategoryCode();
                                     if (code.equals(dishCategoryCode)){
//                                         ProductCategory productCategory=new ProductCategory();
//                                         productCategory.setType(ProductCategory.ITEM);
//                                             products.get(j).setDishCategoryName(name);
//                                             products.get(j).setDishCategoryId(i);
//                                         productCategory.setProduct(products.get(j));
//                                         productCategory.setName(products.get(j).getName());
//                                         productAllCategorys.add(productCategory);
                                        // productCategories.add(productCategory);
                                         productCategorys.get(i).getProducts().add(products.get(j));
                                         productCategorys.get(i).setType(ProductCategory.ITEM);
                                     }
                                 }
                             }
                         
                             itemDishPinHeaderAdapter.setItems(productAllCategorys);
                           // itemDishPinnedListAdapter.notifyDataSetChanged();
                            LogUtil.prinlnLongMsg(TAG,JSON.toJSONString(productAllCategorys));
                          
//                         } catch (Exception e) {
//                             LogUtil.d(TAG,"发生了异常 e:"+e.toString());
//                             e.printStackTrace();
//                         }
                     }
                 }));

        refreshBottomUi();
    }


    /**
     * 显示购物车面板
     */
    private void showShoppingCartPanel() {
        int count = ShoppingCart.getInstance().getTotalQuantity();
        if (count > 0 && !mBottmSheetLayout.isSheetShowing()) {
            mShoppingCartPanel.refreshPanel();
            mBottmSheetLayout.showWithSheetView(mShoppingCartPanel);
        } else {
            mBottmSheetLayout.dismissSheet();
        }
    }
    
    Subscription mSubscription;

    public void onShoppingCartChange(Object o) {
        refreshBottomUi();
        LogUtil.d(TAG,"o:"+o.toString());
        mShoppingCartPanel.refreshPanel();
        itemDishCategoryListAdapter.notifyDataSetChanged();
    //   itemDishPinnedListAdapter.notifyDataSetChanged();
    }

    private void refreshBottomUi() {
        ShoppingCart shoppingCart = ShoppingCart.getInstance();
        int totalCount = shoppingCart.getTotalQuantity();
        double totalPrice = shoppingCart.getTotalPrice();
        if (totalCount>0) {
            tvTotalPrice.setText("共￥" + totalPrice);
            num_tv.setVisibility(View.VISIBLE);
            num_tv.setText(String.valueOf(totalCount));
            ivShopCar.setBackgroundResource(R.drawable.icon_shop_car);
            ivShopCar.setImageDrawable(getResources().getDrawable(R.drawable.icon_shop_car));
        }else{
            num_tv.setVisibility(View.INVISIBLE);
            tvTotalPrice.setText(getString(R.string.tv_shop_empty) );
            ivShopCar.setBackgroundResource(R.drawable.icon_shop_empty);
            ivShopCar.setImageDrawable(getResources().getDrawable(R.drawable.icon_shop_empty));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注册 ---被观察者
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }



    private PopupWindow popupWindow = null;
    ImageView ivIconProduct = null;
    TextView tvDishDesc = null;
    TextView tvDishName = null;
    TextView txtProductMonthSales = null;
    TextView txtProductPrice = null;
    TextView ptvShopSure = null;
    ShoppingCountView shopping_count_view=null;
    public void showPopupWindow(View parent,Product product) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.dialog_dish_product, null);
            ivIconProduct = (ImageView) view.findViewById( R.id.iv_icon_product );
            tvDishDesc = (TextView)view.findViewById( R.id.tv_dish_desc );
            tvDishName = (TextView)view.findViewById( R.id.tv_dish_name );
            txtProductMonthSales = (TextView)view.findViewById( R.id.txt_product_month_sales );
            txtProductPrice = (TextView)view.findViewById( R.id.txt_product_price );
            ptvShopSure = (TextView)view.findViewById( R.id.tvShopSure );
            shopping_count_view=view.findViewById(R.id.shopping_count_view);
            popupWindow = new PopupWindow(view,  
                    windowManager.getDefaultDisplay().getWidth() - CommonUtil.dip2px(mContext,70),
                    LinearLayout.LayoutParams.MATCH_PARENT);
        }
        int num= ShoppingCart.getInstance().getQuantityForProduct(product);
        if (num>0){
            ptvShopSure.setVisibility(View.GONE);
            shopping_count_view.setShoppingCount(num);
            shopping_count_view.setVisibility(View.VISIBLE);
        }else{
            ptvShopSure.setVisibility(View.VISIBLE);
            shopping_count_view.setVisibility(View.GONE);
        }
        AvatarHelper.getInstance().display("http://p0.meituan.net/deal/__16971854__3919079.jpg@380w_214h_1e_1c",ivIconProduct,false);
        tvDishName.setText(product.getName());
        txtProductMonthSales.setText("月售0");
        txtProductPrice.setText("￥"+product.getUnitItems().get(0).getPrice());

        ptvShopSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ptvShopSure.setVisibility(View.GONE);
                ptvShopSure.setEnabled(false);
                if (!ShoppingCart.getInstance().add(product)) {
                }
                shopping_count_view.setShoppingCount(1);
                shopping_count_view.setVisibility(View.VISIBLE);
            }
        });
        shopping_count_view.setOnShoppingClickListener(new ShoppingCountView.ShoppingClickListener() {
            @Override
            public void onAddClick(int num) {
                    if (!ShoppingCart.getInstance().add(product)) {
                    }
            }

            @Override
            public void onMinusClick(int num) {
                if (!ShoppingCart.getInstance().delete(product)) {
                }
                if (num==0){
                   new Handler().postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           ptvShopSure.setVisibility(View.VISIBLE);
                           ptvShopSure.setEnabled(true);
                           shopping_count_view.setVisibility(View.GONE);
                       }
                   },10);
                }
            }
        });
        tvDishDesc.setText(product.getName());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(activity, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
       // popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
        popupWindow .showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        
    }
    
    
    private PopupWindow pListWindow = null;
    TextView mClearTxt;
    ListView lv_data;
    LayoutShoppingCartItemAdapter layoutShoppingCartItemAdapter;
    public void showShopWindow(View parent){
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (pListWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.layout_shopping_cart_panel, null);
            mClearTxt=view.findViewById(R.id.txt_clear);
            lv_data=view.findViewById(R.id.lv_data);
            layoutShoppingCartItemAdapter=new LayoutShoppingCartItemAdapter(mContext);
            lv_data.setAdapter(layoutShoppingCartItemAdapter);
            pListWindow= new PopupWindow(view,
                    windowManager.getDefaultDisplay().getWidth(),
                    windowManager.getDefaultDisplay().getHeight()/2);
            
        }
        ShoppingCart shoppingCart = ShoppingCart.getInstance();
        List<ShoppingEntity> entities = shoppingCart.getShoppingList();
        LogUtil.d("ShopCar", "面板data："+JSON.toJSONString(entities));
        layoutShoppingCartItemAdapter.setItems(entities);
        pListWindow.setFocusable(true);
        pListWindow.setOutsideTouchable(true);
        pListWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(activity, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        pListWindow.setBackgroundDrawable(new BitmapDrawable());
        pListWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        pListWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
    }
    
}
