<p>
<h1 align="center">SACD<font color="#b07219">j</font></h1>
<h6 align="right">v0.6.3</h6>
<h5 align="center">Fater super-resolution fluctuation imaging: SACD reconstruction with FIJI/ImageJ.</h5>
</p>
</br>
<p>
<img src='/imgs/imagej-128.png' align="left" width=120>
</p>


This repository is for Simplified SACD (w/o sparse deconvolution) and will be in continued development. The full SACD can be found in [SACDM](https://github.com/WeisongZhao/SACD2). If you find this useful, please cite the paper. <b>Weisong Zhao et al. SACD: Fater super-resolution fluctuation imaging,  X(X), XXX-XXX (2021)</b>
</br>
</br>
</br>

[Portal]() to the plugin.

## SACD reconstruction
<p align='center'>
<img src='/imgs/.png' align="center" width=700>
</p>


## Declaration
This repository contains the java source code (Maven) for <b>SACD</b> imagej plugin.  This plugin is for the <b>Simplified SACD</b> (w/o sparse deconvolution), and is also accompanied with conventional <b>SOFI</b> calculation. The development of this imagej plugin is work in progress, so expect rough edges. 

If you want to reproduce the results of SACD publication, the <b>SACDM</b> (Matlab version) is recommended. Due to the distance between the Fourier interpolation, deconvolution of <b>SACDj</b>, and <b>SACDM</b>, there may exist a gap between the results of <b>SACDM</b> and <b>SACDj</b>. To me, the implementations of  <b>SACDM</b>  are more flexible and accurate. 


<details>
<summary><b>Plans</b></summary>

- Improve the perfomance of Fourier interpolation;
- Remove redundant code.
- Another type of interpolation, 3D XC type calculation will be added.
- Add sparse deconvolution.
</details>

## Open source [SACDj](https://github.com/WeisongZhao/SACDj)
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.