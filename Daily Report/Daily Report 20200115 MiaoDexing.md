# gitlab 变量使用
## 打开debug logs
默认情况下，GitLab Runner会隐藏处理作业时所做操作的大部分细节。
To enable debug logs (traces), set the CI_DEBUG_TRACE variable to true:
```
job_name:
  variables:
    CI_DEBUG_TRACE: "true"

```
以下是 CI_DEBUG_TRACE 设置为 true 的输出,其中可以看到环境变量的输出:
```
+ eval 'export FF_CMD_DISABLE_DELAYED_ERROR_LEVEL_EXPANSION=$'\''false'\''
554 export FF_USE_LEGACY_BUILDS_DIR_FOR_DOCKER=$'\''false'\''
555 export FF_USE_LEGACY_VOLUMES_MOUNTING_ORDER=$'\''false'\''
556 export CI_RUNNER_SHORT_TOKEN=$'\''UQbnqhaN'\''
557 export CI_BUILDS_DIR=$'\''/home/gitlab-runner/builds'\''
558 export CI_PROJECT_DIR=$'\''/home/gitlab-runner/builds/UQbnqhaN/0/root/01'\''
559 export CI_CONCURRENT_ID=0
560 export CI_CONCURRENT_PROJECT_ID=0
561 export CI_SERVER=$'\''yes'\''
562 export CI_PIPELINE_ID=40
563 export CI_PIPELINE_URL=$'\''http://192.168.0.231:98/root/01/pipelines/40'\''
564 export CI_JOB_ID=106
565 export CI_JOB_URL=$'\''http://192.168.0.231:98/root/01/-/jobs/106'\''
566 export CI_JOB_TOKEN=$'\''[MASKED]'\''
567 export CI_BUILD_ID=106
568 export CI_BUILD_TOKEN=$'\''[MASKED]'\''
569 export CI_REGISTRY_USER=$'\''gitlab-ci-token'\''
570 export CI_REGISTRY_PASSWORD=$'\''[MASKED]'\''
571 export CI_REPOSITORY_URL=$'\''http://gitlab-ci-token:[MASKED]@192.168.0.231:98/root/01.git'\''
572 export CI_JOB_NAME=$'\''job3'\''
573 export CI_JOB_STAGE=$'\''test'\''
574 export CI_NODE_TOTAL=1
575 export CI_BUILD_NAME=$'\''job3'\''
576 export CI_BUILD_STAGE=$'\''test'\''
577 export CI=$'\''true'\''
578 export GITLAB_CI=$'\''true'\''
579 export CI_SERVER_HOST=192.168.0.231
580 export CI_SERVER_NAME=$'\''GitLab'\''
581 export CI_SERVER_VERSION=12.6.2
582 export CI_SERVER_VERSION_MAJOR=12
583 export CI_SERVER_VERSION_MINOR=6
584 export CI_SERVER_VERSION_PATCH=2
585 export CI_SERVER_REVISION=$'\''3041661dec5'\''
586 export GITLAB_FEATURES='\'''\''
587 export CI_PROJECT_ID=3
588 export CI_PROJECT_NAME=01
589 export CI_PROJECT_TITLE=01
590 export CI_PROJECT_PATH=$'\''root/01'\''
591 export CI_PROJECT_PATH_SLUG=$'\''root-01'\''
592 export CI_PROJECT_NAMESPACE=$'\''root'\''
593 export CI_PROJECT_URL=$'\''http://192.168.0.231:98/root/01'\''
594 export CI_PROJECT_VISIBILITY=$'\''public'\''
595 export CI_PROJECT_REPOSITORY_LANGUAGES='\'''\''
596 export CI_DEFAULT_BRANCH=$'\''master'\''
597 export CI_PAGES_DOMAIN=$'\''example.com'\''
598 export CI_PAGES_URL=$'\''http://root.example.com/01'\''
599 export CI_REGISTRY=$'\''192.168.0.231:98'\''
600 export CI_REGISTRY_IMAGE=$'\''192.168.0.231:98/root/01'\''
601 export CI_API_V4_URL=$'\''http://192.168.0.231:98/api/v4'\''
602 export CI_PIPELINE_IID=14
603 export CI_PIPELINE_SOURCE=$'\''push'\''
604 export CI_CONFIG_PATH=$'\''.gitlab-ci.yml'\''
605 export CI_COMMIT_SHA=$'\''8bab787b2c4106147a96e6dad9ceada294404aa1'\''
606 export CI_COMMIT_SHORT_SHA=$'\''8bab787b'\''
607 export CI_COMMIT_BEFORE_SHA=$'\''08d17111d755ef2ed7f11491203f7915954b43a1'\''
608 export CI_COMMIT_REF_NAME=$'\''master'\''
609 export CI_COMMIT_REF_SLUG=$'\''master'\''
610 export CI_COMMIT_BRANCH=$'\''master'\''
611 export CI_COMMIT_MESSAGE=$'\''Update .gitlab-ci.yml'\''
612 export CI_COMMIT_TITLE=$'\''Update .gitlab-ci.yml'\''
613 export CI_COMMIT_DESCRIPTION='\'''\''
614 export CI_COMMIT_REF_PROTECTED=$'\''true'\''
615 export CI_BUILD_REF=$'\''8bab787b2c4106147a96e6dad9ceada294404aa1'\''
616 export CI_BUILD_BEFORE_SHA=$'\''08d17111d755ef2ed7f11491203f7915954b43a1'\''
617 export CI_BUILD_REF_NAME=$'\''master'\''
618 export CI_BUILD_REF_SLUG=$'\''master'\''
619 export CI_RUNNER_ID=5
620 export CI_RUNNER_DESCRIPTION=$'\''Test 01'\''
621 export CI_RUNNER_TAGS=$'\''mdx'\''
622 export CI_DEBUG_TRACE=$'\''true'\''
623 export GITLAB_USER_ID=1
624 export GITLAB_USER_EMAIL=$'\''admin@example.com'\''
625 export GITLAB_USER_LOGIN=$'\''root'\''
626 export GITLAB_USER_NAME=$'\''Administrator'\''
627 export CI_SHARED_ENVIRONMENT=$'\''true'\''
628 export CI_RUNNER_VERSION=$'\''12.5.0~beta.2011.gac28d7b4'\''
629 export CI_RUNNER_REVISION=$'\''ac28d7b4'\''
630 export CI_RUNNER_EXECUTABLE_ARCH=$'\''linux/386'\''
631 $'\''cd'\'' "/home/gitlab-runner/builds/UQbnqhaN/0/root/01"
632 '
633 ++ export FF_CMD_DISABLE_DELAYED_ERROR_LEVEL_EXPANSION=false
634 ++ FF_CMD_DISABLE_DELAYED_ERROR_LEVEL_EXPANSION=false
635 ++ export FF_USE_LEGACY_BUILDS_DIR_FOR_DOCKER=false
636 ++ FF_USE_LEGACY_BUILDS_DIR_FOR_DOCKER=false
637 ++ export FF_USE_LEGACY_VOLUMES_MOUNTING_ORDER=false
638 ++ FF_USE_LEGACY_VOLUMES_MOUNTING_ORDER=false
639 ++ export CI_RUNNER_SHORT_TOKEN=UQbnqhaN
640 ++ CI_RUNNER_SHORT_TOKEN=UQbnqhaN
641 ++ export CI_BUILDS_DIR=/home/gitlab-runner/builds
642 ++ CI_BUILDS_DIR=/home/gitlab-runner/builds
643 ++ export CI_PROJECT_DIR=/home/gitlab-runner/builds/UQbnqhaN/0/root/01
644 ++ CI_PROJECT_DIR=/home/gitlab-runner/builds/UQbnqhaN/0/root/01
645 ++ export CI_CONCURRENT_ID=0
646 ++ CI_CONCURRENT_ID=0
647 ++ export CI_CONCURRENT_PROJECT_ID=0
648 ++ CI_CONCURRENT_PROJECT_ID=0
649 ++ export CI_SERVER=yes
650 ++ CI_SERVER=yes
651 ++ export CI_PIPELINE_ID=40
652 ++ CI_PIPELINE_ID=40
653 ++ export CI_PIPELINE_URL=http://192.168.0.231:98/root/01/pipelines/40
654 ++ CI_PIPELINE_URL=http://192.168.0.231:98/root/01/pipelines/40
655 ++ export CI_JOB_ID=106
656 ++ CI_JOB_ID=106
657 ++ export CI_JOB_URL=http://192.168.0.231:98/root/01/-/jobs/106
658 ++ CI_JOB_URL=http://192.168.0.231:98/root/01/-/jobs/106
659 ++ export CI_JOB_TOKEN=[MASKED]
660 ++ CI_JOB_TOKEN=[MASKED]
661 ++ export CI_BUILD_ID=106
662 ++ CI_BUILD_ID=106
663 ++ export CI_BUILD_TOKEN=[MASKED]
664 ++ CI_BUILD_TOKEN=[MASKED]
665 ++ export CI_REGISTRY_USER=gitlab-ci-token
666 ++ CI_REGISTRY_USER=gitlab-ci-token
667 ++ export CI_REGISTRY_PASSWORD=[MASKED]
668 ++ CI_REGISTRY_PASSWORD=[MASKED]
669 ++ export CI_REPOSITORY_URL=http://gitlab-ci-token:[MASKED]@192.168.0.231:98/root/01.git
670 ++ CI_REPOSITORY_URL=http://gitlab-ci-token:[MASKED]@192.168.0.231:98/root/01.git
671 ++ export CI_JOB_NAME=job3
672 ++ CI_JOB_NAME=job3
673 ++ export CI_JOB_STAGE=test
674 ++ CI_JOB_STAGE=test
675 ++ export CI_NODE_TOTAL=1
676 ++ CI_NODE_TOTAL=1
677 ++ export CI_BUILD_NAME=job3
678 ++ CI_BUILD_NAME=job3
679 ++ export CI_BUILD_STAGE=test
680 ++ CI_BUILD_STAGE=test
681 ++ export CI=true
682 ++ CI=true
683 ++ export GITLAB_CI=true
684 ++ GITLAB_CI=true
685 ++ export CI_SERVER_HOST=192.168.0.231
686 ++ CI_SERVER_HOST=192.168.0.231
687 ++ export CI_SERVER_NAME=GitLab
688 ++ CI_SERVER_NAME=GitLab
689 ++ export CI_SERVER_VERSION=12.6.2
690 ++ CI_SERVER_VERSION=12.6.2
691 ++ export CI_SERVER_VERSION_MAJOR=12
692 ++ CI_SERVER_VERSION_MAJOR=12
693 ++ export CI_SERVER_VERSION_MINOR=6
694 ++ CI_SERVER_VERSION_MINOR=6
695 ++ export CI_SERVER_VERSION_PATCH=2
696 ++ CI_SERVER_VERSION_PATCH=2
697 ++ export CI_SERVER_REVISION=3041661dec5
698 ++ CI_SERVER_REVISION=3041661dec5
699 ++ export GITLAB_FEATURES=
700 ++ GITLAB_FEATURES=
701 ++ export CI_PROJECT_ID=3
702 ++ CI_PROJECT_ID=3
703 ++ export CI_PROJECT_NAME=01
704 ++ CI_PROJECT_NAME=01
705 ++ export CI_PROJECT_TITLE=01
706 ++ CI_PROJECT_TITLE=01
707 ++ export CI_PROJECT_PATH=root/01
708 ++ CI_PROJECT_PATH=root/01
709 ++ export CI_PROJECT_PATH_SLUG=root-01
710 ++ CI_PROJECT_PATH_SLUG=root-01
711 ++ export CI_PROJECT_NAMESPACE=root
712 ++ CI_PROJECT_NAMESPACE=root
713 ++ export CI_PROJECT_URL=http://192.168.0.231:98/root/01
714 ++ CI_PROJECT_URL=http://192.168.0.231:98/root/01
715 ++ export CI_PROJECT_VISIBILITY=public
716 ++ CI_PROJECT_VISIBILITY=public
717 ++ export CI_PROJECT_REPOSITORY_LANGUAGES=
718 ++ CI_PROJECT_REPOSITORY_LANGUAGES=
719 ++ export CI_DEFAULT_BRANCH=master
720 ++ CI_DEFAULT_BRANCH=master
721 ++ export CI_PAGES_DOMAIN=example.com
722 ++ CI_PAGES_DOMAIN=example.com
723 ++ export CI_PAGES_URL=http://root.example.com/01
724 ++ CI_PAGES_URL=http://root.example.com/01
725 ++ export CI_REGISTRY=192.168.0.231:98
726 ++ CI_REGISTRY=192.168.0.231:98
727 ++ export CI_REGISTRY_IMAGE=192.168.0.231:98/root/01
728 ++ CI_REGISTRY_IMAGE=192.168.0.231:98/root/01
729 ++ export CI_API_V4_URL=http://192.168.0.231:98/api/v4
730 ++ CI_API_V4_URL=http://192.168.0.231:98/api/v4
731 ++ export CI_PIPELINE_IID=14
732 ++ CI_PIPELINE_IID=14
733 ++ export CI_PIPELINE_SOURCE=push
734 ++ CI_PIPELINE_SOURCE=push
735 ++ export CI_CONFIG_PATH=.gitlab-ci.yml
736 ++ CI_CONFIG_PATH=.gitlab-ci.yml
737 ++ export CI_COMMIT_SHA=8bab787b2c4106147a96e6dad9ceada294404aa1
738 ++ CI_COMMIT_SHA=8bab787b2c4106147a96e6dad9ceada294404aa1
739 ++ export CI_COMMIT_SHORT_SHA=8bab787b
740 ++ CI_COMMIT_SHORT_SHA=8bab787b
741 ++ export CI_COMMIT_BEFORE_SHA=08d17111d755ef2ed7f11491203f7915954b43a1
742 ++ CI_COMMIT_BEFORE_SHA=08d17111d755ef2ed7f11491203f7915954b43a1
743 ++ export CI_COMMIT_REF_NAME=master
744 ++ CI_COMMIT_REF_NAME=master
745 ++ export CI_COMMIT_REF_SLUG=master
746 ++ CI_COMMIT_REF_SLUG=master
747 ++ export CI_COMMIT_BRANCH=master
748 ++ CI_COMMIT_BRANCH=master
749 ++ export 'CI_COMMIT_MESSAGE=Update .gitlab-ci.yml'
750 ++ CI_COMMIT_MESSAGE='Update .gitlab-ci.yml'
751 ++ export 'CI_COMMIT_TITLE=Update .gitlab-ci.yml'
752 ++ CI_COMMIT_TITLE='Update .gitlab-ci.yml'
753 ++ export CI_COMMIT_DESCRIPTION=
754 ++ CI_COMMIT_DESCRIPTION=
755 ++ export CI_COMMIT_REF_PROTECTED=true
756 ++ CI_COMMIT_REF_PROTECTED=true
757 ++ export CI_BUILD_REF=8bab787b2c4106147a96e6dad9ceada294404aa1
758 ++ CI_BUILD_REF=8bab787b2c4106147a96e6dad9ceada294404aa1
759 ++ export CI_BUILD_BEFORE_SHA=08d17111d755ef2ed7f11491203f7915954b43a1
760 ++ CI_BUILD_BEFORE_SHA=08d17111d755ef2ed7f11491203f7915954b43a1
761 ++ export CI_BUILD_REF_NAME=master
762 ++ CI_BUILD_REF_NAME=master
763 ++ export CI_BUILD_REF_SLUG=master
764 ++ CI_BUILD_REF_SLUG=master
765 ++ export CI_RUNNER_ID=5
766 ++ CI_RUNNER_ID=5
767 ++ export 'CI_RUNNER_DESCRIPTION=Test 01'
768 ++ CI_RUNNER_DESCRIPTION='Test 01'
769 ++ export CI_RUNNER_TAGS=mdx
770 ++ CI_RUNNER_TAGS=mdx
771 ++ export CI_DEBUG_TRACE=true
772 ++ CI_DEBUG_TRACE=true
773 ++ export GITLAB_USER_ID=1
774 ++ GITLAB_USER_ID=1
775 ++ export GITLAB_USER_EMAIL=admin@example.com
776 ++ GITLAB_USER_EMAIL=admin@example.com
777 ++ export GITLAB_USER_LOGIN=root
778 ++ GITLAB_USER_LOGIN=root
779 ++ export GITLAB_USER_NAME=Administrator
780 ++ GITLAB_USER_NAME=Administrator
781 ++ export CI_SHARED_ENVIRONMENT=true
782 ++ CI_SHARED_ENVIRONMENT=true
783 ++ export CI_RUNNER_VERSION=12.5.0~beta.2011.gac28d7b4
784 ++ CI_RUNNER_VERSION=12.5.0~beta.2011.gac28d7b4
785 ++ export CI_RUNNER_REVISION=ac28d7b4
786 ++ CI_RUNNER_REVISION=ac28d7b4
787 ++ export CI_RUNNER_EXECUTABLE_ARCH=linux/386
788 ++ CI_RUNNER_EXECUTABLE_ARCH=linux/386

```
