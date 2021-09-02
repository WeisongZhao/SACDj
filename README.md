
[![website](https://img.shields.io/badge/website-up-green.svg)](https://weisongzhao.github.io/SACDj/)
[![paper](https://img.shields.io/badge/paper-science-black.svg)](https://www.science.org/)
[![Github commit](https://img.shields.io/github/last-commit/WeisongZhao/SACDj)](https://github.com/WeisongZhao/SACDj/)
[![License](https://img.shields.io/github/license/WeisongZhao/SACDj)](https://github.com/WeisongZhao/SACDj/blob/master/LICENSE/)<br>
[![Twitter](https://img.shields.io/twitter/follow/weisong_zhao?label=weisong)](https://twitter.com/weisong_zhao/status/1370308101690118146)
[![GitHub watchers](https://img.shields.io/github/watchers/WeisongZhao/SACDj?style=social)](https://github.com/WeisongZhao/SACDj/) 
[![GitHub stars](https://img.shields.io/github/stars/WeisongZhao/SACDj?style=social)](https://github.com/WeisongZhao/SACDj/) 
[![GitHub forks](https://img.shields.io/github/forks/WeisongZhao/SACDj?style=social)](https://github.com/WeisongZhao/SACDj/)



<p>
<h1 align="center">SACD<font color="#b07219">j</font></h1>
<h6 align="right">v1.1.3</h6>
<h5 align="center">Fater super-resolution fluctuation imaging: SACD reconstruction with FIJI/ImageJ.</h5>
</p>
<br>

<p>
<img src='./imgs/splash.png' align="left" width=160>
<p>


This repository is for Simplified SACD (w/o sparse deconvolution) and will be in continued development. The full SACD can be found in [SACDm](https://github.com/WeisongZhao/SACDm). It is a part of publication. For details, please refer to: [Weisong Zhao et al. Enhancing detectable fluorescence fluctuation for fast high-throughput and four-dimensional live-cell super-resolution imaging, Science, X, XXX-XXX (2021)](https://www.science.org/). Please cite PANEL in your publications, if it helps your research.
<br>
<br>
<br>

<p>
<img src='./imgs/imagej-128.png' align="right" width=50>
</p>
<br>

[Portal](https://github.com/WeisongZhao/SACDj/blob/main/SACDj_-1.1.3.jar) to the plugin.

## SACD reconstruction

<p align='center'>
<img src='./imgs/Fig1.png' align="center" width=900>
</p>


## Declaration
This repository contains the java source code (Maven) for <b>SACD</b> imagej plugin.  This plugin is for the <b>Simplified SACD</b> (w/o sparse deconvolution), and is also accompanied with conventional <b>SOFI reconstruction</b> ; <b>RL deconvolution</b>; and <b>PSF calculation</b> features. The development of this imagej plugin is work in progress, so expect rough edges. 

If you want to reproduce the results of SACD publication, the [SACDm](https://github.com/WeisongZhao/SACDm) (Matlab version) is recommended. Due to the distance between the Fourier interpolation, deconvolution of <b>SACDj</b>, and <b>SACDm</b>, there may exist a gap between the results of <b>SACDm</b> and <b>SACDj</b>. To me, the implementations of <b>SACDm</b> are more flexible and accurate. 


<details>
<summary><b>Plans</b></summary>

- Improve the perfomance of Fourier interpolation;
- Remove redundant code and reconsitution ugly code. (in progress)
- ~~Accelarated RL deconvolution.~~
- ~~Accelarated RL-TV deconvolution.~~
- Another type of interpolation, 3D XC type calculation will be added.
- Add sparse deconvolution.
</details>

## Open source [SACDj](https://github.com/WeisongZhao/SACDj)
This software and corresponding methods can only be used for **non-commercial** use, and they are under Open Data Commons Open Database License v1.0.