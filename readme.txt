2014-07-04 LouisPeng
尚未加入ANT自动编译，修改后，需要手动打包

手动打包步骤：
1.root目录新建lib文件夹，把libs中的jar都挪过去
2.build path里面import lib里面所有的包，去掉去掉export里lib里面对应的jar前面的钩
3.用手Q的keystore export出来（/AndroidQQ_Lite_proj/QQLite/android-release-key.ke kgs)

    <!-- 签名文件设置，请将签名文件放到SVN上，并在这里指定签名文件，建议将签名文件放到构建脚本同级目录，${project_path}即为工作目录-->
    <property name="keystore" value="buildOption/android-release-key.keystore"/>
    <!-- 签名的密钥名称设置-->
    <property name="key_name" value="androidreleasekey"/>
    <!-- 签名的密码设置-->
    <property name="key_pass" value="luozhifan@tencent-2010"/>
    <!-- 签名的密码设置-->
    <property name="store_pass" value="luozhifan@tencent-2010"/>
