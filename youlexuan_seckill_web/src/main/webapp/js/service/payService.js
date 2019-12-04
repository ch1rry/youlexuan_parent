app.service('payService',function ($http) {
    //请求预下单
    this.createNative=function () {
      return  $http.get('pay/crateNative.do');
    }

    //查询指定订单编号的交易状态
    this.queryPayStatus=function (out_trade_no) {
     return   $http.get('pay/queryPayStatus.do?out_trade_no='+out_trade_no);
    }
})
