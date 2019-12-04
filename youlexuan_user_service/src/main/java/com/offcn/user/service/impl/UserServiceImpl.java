package com.offcn.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbUserMapper;
import com.offcn.pojo.TbUser;
import com.offcn.pojo.TbUserExample;
import com.offcn.pojo.TbUserExample.Criteria;
import com.offcn.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户表服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination smsQueue;

	@Value("${singName}")
	private String singName;

	@Value("${templateId}")
	private String templateId;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {

		user.setCreated(new Date());
		user.setUpdated(new Date());
		//密码md5加密
		String md5Hex = DigestUtils.md5Hex(user.getPassword());
		user.setPassword(md5Hex);
		userMapper.insert(user);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();

		if(user!=null){
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
		}

		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void createSmsCode(String mobile) {
		//随机生成验证码 5位整数
		String code=(long)(Math.random()*1000000)+"";
		System.out.println("随机生成验证码:"+code);

		//把验证码保存redis缓存
        redisTemplate.boundHashOps("smscode").put(mobile,code);



        //把验证码 发送短信给指定手机号码
        jmsTemplate.send(smsQueue, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage map = session.createMapMessage();

				map.setString("mobile",mobile);
				map.setString("singName",singName);
				map.setString("templateId",templateId);
				Map m=new HashMap();
				m.put("code",code);
				map.setString("templateParam", JSON.toJSONString(m));
				return map;
			}
		});



	}

	@Override
	public boolean checkSmsCode(String mobile, String smscode) {

		//根据手机号码从redis获取
	String smscodeSrc=(String) 	redisTemplate.boundHashOps("smscode").get(mobile);

	if(smscodeSrc==null){
		return false;
	}

	if(!smscodeSrc.equals(smscode)){
		return  false;
	}

		return true;
	}
}
