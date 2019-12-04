app.controller('seckillGoodsController',function ($scope,$location,$interval,seckillGoodsService) {

    //获取当前正在秒杀的全部商品
    $scope.findList=function () {
        seckillGoodsService.findList().success(function (response) {
            $scope.list=response;
        })
    }

    //根据id获取秒杀商品信息
    $scope.findOne=function () {
     var id=   $location.search()['id'];
     seckillGoodsService.findOne(id).success(function (response) {
         $scope.entity=response;

         //计算当前时间距离秒杀结束时间剩余的毫秒
      $scope.allSecond= Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000) ;

         //倒计时
         var time= $interval(function () {
             if($scope.allSecond>0){
                 $scope.allSecond=$scope.allSecond-1;
               $scope.times= convertTimeString($scope.allSecond);
             }else {
                 alert("倒计时结束");
                 $interval.cancel(time);
             }
         },1000)
     })
    }

    //跳转到商品详情页
    $scope.goItem=function (id) {
        location.href="seckill-item.html#?id="+id;
    }


//转换秒数为 格式化后日期
    convertTimeString=function (allSecond) {
        //计算剩余天数
     var days=  Math.floor(allSecond/(60*60*24));
     //计算剩余小时
     var hours=  Math.floor( (allSecond-days*(60*60*24))/(60*60));
    //计算剩余分
        var minutes=  Math.floor( (allSecond-days*(60*60*24)-hours*(60*60))/(60));
        //计算剩余秒数
        var seconds=Math.floor( (allSecond-days*(60*60*24)-hours*(60*60)-minutes*60));

        var str="";

        if(days>0){
            str+=days+"天 ";
        }else {
            str+" ";
        }

        if(hours>0){
            str+=" "+hours+":";
        }

     return str+minutes+":"+seconds;

    }


    //秒杀下单
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if(response.success){
                //秒杀抢购成功，跳转到支付页面
                location.href="pay.html";
            }else {
                if(response.message=='用户未登录'){
                    //用户未登录跳转到登录页面
                    location.href="login.html";
                }else {
                    alert(response.message);
                }
            }
        })
    }

})
