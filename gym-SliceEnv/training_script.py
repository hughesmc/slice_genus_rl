#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jun  3 16:37:47 2021

@author: markhughes
"""

import ray
from ray.tune.registry import register_env
import os
import ray.rllib.agents.ppo as ppo
import shutil
from gym_SliceEnv.envs.SliceEnv_env import SliceEnv
import sys
import datetime


if len(sys.argv)>1:
	knot=sys.argv[1]
else:
	knot="random"
	
if len(sys.argv)>2:
	n_iter=int(sys.argv[2])
else:
	n_iter=10000
	
time=str(datetime.datetime.now())
print("time = ",time)
time=time.replace("-","_")
time=time.replace(":","_")
time=time.replace(".","_")
time=time.replace(" ","_")


ray.init(ignore_reinit_error=True)

def env_creator(env_config):
    return SliceEnv(env_config)  # return an env instance

config = ppo.DEFAULT_CONFIG.copy()
config["log_level"] = "WARN"
config["env_config"] = {"max_action_count": 250,
                       "starting_word": knot,
                       "inaction_penalty": 0.05,
                       "final_penalty": 100}


register_env("my_env", env_creator)
agent = ppo.PPOTrainer(env="my_env",config=config)


chkpt_root = "tmp/exa_"+knot+"_"+time
shutil.rmtree(chkpt_root, ignore_errors=True, onerror=None)

# init directory in which to log results
ray_results = "{}/ray_results/".format(os.getenv("HOME"))
shutil.rmtree(ray_results, ignore_errors=True, onerror=None)


# start Ray -- add `local_mode=True` here for debugging
#ray.init(ignore_reinit_error=True)

# register the custom environment
#select_env = "SliceEnv-v0"
#register_env(select_env, lambda config: SliceEnv())


# configure the environment and create agent
#config = ppo.DEFAULT_CONFIG.copy()
#config["log_level"] = "WARN"
#config["env_config"] = {"max_action_count": 13}
#agent = ppo.PPOTrainer(config, env=select_env)

status = "{:2d} reward {:6.2f}/{:6.2f}/{:6.2f} len {:4.2f} saved {}"

# train a policy with RLlib using PPO
for n in range(n_iter):
    result = agent.train()
    chkpt_file = agent.save(chkpt_root)

    print(status.format(n + 1,
                result["episode_reward_min"],
                result["episode_reward_mean"],
                result["episode_reward_max"],
                result["episode_len_mean"],
                chkpt_file
                ))
