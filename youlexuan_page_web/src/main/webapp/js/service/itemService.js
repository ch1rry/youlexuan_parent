app.service('itemService',function ($http) {
    //添加商品到购物车
    this.addGoodsToCart=function (itemId, num) {
        return   $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+itemId+"&num="+num,{'withCredentials':true});
    }
})
