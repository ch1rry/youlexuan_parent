app.controller('payController',function ($scope,$location,payService) {

    //发出预下单请求方法
    $scope.createNative=function () {
        payService.createNative().success(function (response) {

            //获取预下单响应内容

            $scope.total_fee=(response.total_fee/100).toFixed(2);
            $scope.out_trade_no=response.out_trade_no;

           var qr= new QRious({
                element:document.getElementById('erweima'),
                size:250,
                value:response.qrcode
            });

           //查询交易状态
            queryPayStatus($scope.out_trade_no);

        })
    }

    //查询指定订单编号的交易状态
    queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if(response.success){
                location.href="paysuccess.html#?total_fee="+$scope.total_fee;
            }else {
                if(response.message=='超过时间未支付,订单取消'){
                   // alert('二维码查询超时');
                    //document.getElementById('msg').innerText="二维码已过期，刷新页面重新获取二维码。";
                    alert("对不起5分钟内你没有完成支付，你的订单已经被取消");
                    location.href="payfail.html";
                }else {
                    location.href="payfail.html";
                }

            }
        })
    }

    //获取页面传递支付成功金额
    $scope.loadMoney=function () {
      return $location.search()['total_fee'];
    }
})
