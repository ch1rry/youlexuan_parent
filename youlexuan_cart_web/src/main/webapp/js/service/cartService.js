app.service('cartService',function ($http) {
    //获取购物车数据
    this.findCartList=function () {
      return  $http.get('cart/findCartList.do');
    }

    //添加商品到购物车
    this.addGoodsToCartList=function (itemId, num) {
      return  $http.get('cart/addGoodsToCartList.do?itemId='+itemId+"&num="+num);
    }

    //计算购物车合计金额、总数量
    this.sum=function (cartList) {
        //定义一个实体对象存储合计金额和总数量
        var totalObject={totalMoney:0.0,totalNum:0};

        //遍历购物车
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];
            //遍历每个商家购物明细
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem=cart.orderItemList[j];
                //合计购买数量
                totalObject.totalNum+=orderItem.num;
                //合计总金额
                totalObject.totalMoney+=orderItem.totalFee;
            }
        }

        return totalObject;
    }

    //获取收货地址列表
    this.findAddressList=function(){
        return $http.get('address/findListByLoginUser.do');
    }

    //保存订单
    this.submitOrder=function (order) {
        return $http.post('order/add.do',order);
    }
})
