package Reasoner;

import Sampler.Action;
import Sampler.Sample;

import java.util.List;


public interface IModel {
  Reasoning CalculateReasoning(Sample s); //should calculate the most likely action to occur (which is above a certain threshold)
  void TakeFeedback(Reasoning wrongReasoning); //used to update the reasoners model based on two wrong actions
}
