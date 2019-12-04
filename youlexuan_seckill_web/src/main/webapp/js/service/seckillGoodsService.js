app.service('seckillGoodsService',function ($http) {
    //获取当前正在秒杀的全部商品
    this.findList=function () {
      return  $http.get('seckillGoods/findList.do');
    }

    //获取指定id秒杀商品详细信息
    this.findOne=function (id) {
     return   $http.get('seckillGoods/findOneFromRedis.do?id='+id);
    }

    //秒杀下单
    this.submitOrder=function (seckillId) {
      return  $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }
})
