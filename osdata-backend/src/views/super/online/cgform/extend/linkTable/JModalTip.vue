<template>
  <div :class="['jeecg-update-tip-bar', {'mobile': getIsMobile}]" v-if="flag">
      <div class="container">
          <div class="outer">
              <div class="inner">
                  <span class="tip">正在修改表单数据 ···</span>
                  <div class="cancel" v-on:click="$emit('cancel')">取消</div>
                  <div class="save" v-on:click="$emit('save')">保存</div>
              </div>
          </div>
      </div>
  </div>
</template>

<script>
    import {computed} from 'vue'
    import {useAppInject} from '/@/hooks/web/useAppInject'
  export default {
    name: 'JModalTip',
    props:{
      visible: {
        type: Boolean,
        default: false
      }
    },
    emits: ['save', 'cancel'],
    setup(props){
      const {getIsMobile} = useAppInject()

      const flag = computed(()=>{
        return props.visible
      });
      
      return {
        flag,
        getIsMobile,
      }
    }
  };
</script>

<style scoped lang="less">
  .jeecg-update-tip-bar {
      width: calc(100% - 100px);
      font-size: 14px;
      .container{
          position: relative;
          margin: 0 auto;
          .outer {
              position: absolute;
              display: flex;
              -webkit-box-align: center;
              align-items: center;
              -webkit-box-pack: center;
              justify-content: center;
              width: 100%;
              z-index: 11;
              top: -30px;
              .inner {
                  height: 42px;
                  border-radius: 42px;
                  color: rgb(255, 255, 255);
                  background-color: rgb(33, 150, 243);
                  line-height: 42px;
                  padding: 0px 10px 0px 24px;
                  z-index: 9;
                  box-shadow: rgb(0 0 0 / 12%) 0px 6px 24px, rgb(0 0 0 / 8%) 0px 2px 4px;
                  display: flex;
                  flex-direction: row;
                  -webkit-box-align: center;
                  align-items: center;
                  min-width: 380px;
                  .tip {
                      -webkit-flex: 1;
                      flex: 1 1 0%;
                      -ms-flex: 1;
                      font-weight: 700;
                  }
                  .cancel {
                      cursor: pointer;
                      display: inline-block;
                      height: 28px;
                      line-height: 28px;
                      padding: 0px 18px;
                      border-radius: 28px;
                      color: rgb(255, 255, 255);
                      margin-left: 30px !important;
                      margin-right: 10px !important;
                      &:hover {
                          background-color: rgba(255, 255, 255, 0.16);
                      }
                  }

                  .save {
                      cursor: pointer;
                      display: inline-block;
                      height: 28px;
                      line-height: 28px;
                      padding: 0px 18px;
                      border-radius: 28px;
                      color: rgb(62, 164, 252);
                      background-color: rgb(255, 255, 255);
                      font-weight: 600;
                      &:hover {
                          background-color: rgb(226, 241, 254);
                      }
                  }
              }
          }
      }
  }

  // 【QQYUN-7481】【移动端ONLINE表单】关联记录列表页修改，确认修改tip展示未居中
  .mobile.jeecg-update-tip-bar {
    .container {
      .outer {
        z-index: 99999;
        left: 50px;

        .inner {
          min-width: 360px;
        }
      }
    }
  }

</style>
