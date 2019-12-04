app.controller('itemController',function ($scope,itemService) {


    //点击 加号或者减号 改变购买数量

    $scope.updateNum=function (num) {
        $scope.num=$scope.num+num;

        if($scope.num<1){
            $scope.num=1;
        }
    }

    //记录用户所选择的规格选项
    $scope.specificationItems={};

    //用户选择规格选项触发方法
    //｛"网络":"电信4G"，"机身内存":"128G"｝
    $scope.selectSpecification=function (name, value) {
        $scope.specificationItems[name]=value;

        searchSku();
    }

    //判断指定规格和规格选项是否被选中
    $scope.isSelected=function (name, value) {
        if($scope.specificationItems[name]==value){
            return true;
        }else {

            return false;
        }
    }

    //读取sku列表方法
    $scope.loadSku=function () {
        $scope.sku= skuList[0];
        //设置sku规格，设置默认选中规格(深克隆)
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //比对2个json对象，看是否相同
    matchObject=function (json1, json2) {
        //遍历json1，逐个和json2元素比对
        for(var k in json1){
            if(json1[k]!=json2[k]){
                return false;
            }
        }

        //遍历json2，逐个和json1比对
        for(var j in json2){
            if(json2[j]!=json1[j]){
                return false;
            }
        }

        return true;


    }

    //根据用户选中的规格选项比对对应的sku对象
    searchSku=function () {
        //遍历sku集合
        for(var i=0;i<skuList.length;i++){

            //比对每个sku对象里面规格 和 用户选中的规格
            if(matchObject(skuList[i].spec,$scope.specificationItems)){
                $scope.sku=skuList[i];
            }

        }
    }

    //添加购物车方法
    $scope.addtoCart=function () {
        // alert("添加到购物车成功!")
        itemService.addGoodsToCart($scope.sku.id,$scope.num).success(function (response) {
            if(response.success){
                //跳转到购物车
                location.href="http://localhost:9108/cart.html";
            }else {
                alert(response.message);
            }
        });
    }


})
